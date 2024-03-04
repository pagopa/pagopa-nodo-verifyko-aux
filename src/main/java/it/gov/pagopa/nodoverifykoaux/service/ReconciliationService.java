package it.gov.pagopa.nodoverifykoaux.service;

import com.google.gson.Gson;
import it.gov.pagopa.nodoverifykoaux.entity.ColdStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.entity.HotStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.exception.AppError;
import it.gov.pagopa.nodoverifykoaux.exception.AppException;
import it.gov.pagopa.nodoverifykoaux.mapper.ConvertColdStorageVerifyKOToHotStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.model.ConvertedKey;
import it.gov.pagopa.nodoverifykoaux.model.action.*;
import it.gov.pagopa.nodoverifykoaux.repository.BlobStorageRepository;
import it.gov.pagopa.nodoverifykoaux.repository.DataStorageRepository;
import it.gov.pagopa.nodoverifykoaux.repository.TableStorageRepository;
import it.gov.pagopa.nodoverifykoaux.util.CommonUtility;
import it.gov.pagopa.nodoverifykoaux.util.DateValidator;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReconciliationService {

    private final DataStorageRepository hotStorageRepo;

    private final TableStorageRepository coldStorageRepo;

    private final BlobStorageRepository coldStorageBlobRepo;

    private final DateValidator dateValidator;

    private final ModelMapper mapper;

    private final Gson gsonMapper;

    public ReconciliationService(DataStorageRepository hotStorageRepo,
                                 TableStorageRepository coldStorageRepo,
                                 BlobStorageRepository coldStorageBlobRepo,
                                 ModelMapper mapper) {
        this.hotStorageRepo = hotStorageRepo;
        this.coldStorageRepo = coldStorageRepo;
        this.coldStorageBlobRepo = coldStorageBlobRepo;
        this.dateValidator = new DateValidator("yyyy-MM-ddZ");
        this.mapper = mapper;
        this.gsonMapper = new Gson();
    }

    private static ReconciliationStatus generateReconciliationStatus(List<ReconciledEventStatus> coldToHotReconciledEvents, List<ReconciledEventStatus> hotToColdReconciledEvents, Date startTime, String date, String stringedDate) {
        Date endTime = Calendar.getInstance().getTime();
        long coldToHotSuccess = coldToHotReconciledEvents.stream().filter(ev -> ReconciledEventState.SUCCESS.equals(ev.getStatus())).count();
        long hotToColdSuccess = hotToColdReconciledEvents.stream().filter(ev -> ReconciledEventState.SUCCESS.equals(ev.getStatus())).count();
        return ReconciliationStatus.builder()
                .statistics(ReconciliationStatistics.builder()
                        .startedAt(startTime)
                        .endedAt(endTime)
                        .analyzed(ReconciliationHotColdComparation.builder()
                                .fromColdToHot(coldToHotReconciledEvents.size())
                                .fromHotToCold(hotToColdReconciledEvents.size())
                                .build())
                        .succeeded(ReconciliationHotColdComparation.builder()
                                .fromColdToHot((int) (coldToHotSuccess))
                                .fromHotToCold((int) (hotToColdSuccess))
                                .build())
                        .failed(ReconciliationHotColdComparation.builder()
                                .fromColdToHot((int) (coldToHotReconciledEvents.size() - coldToHotSuccess))
                                .fromHotToCold((int) (hotToColdReconciledEvents.size() - hotToColdSuccess))
                                .build())
                        .build())
                .overview(ReconciliationData.builder()
                        .date(date)
                        .usedDateForSearch(stringedDate)
                        .fromColdToHotStorage(coldToHotReconciledEvents)
                        .fromHotToColdStorage(hotToColdReconciledEvents)
                        .build())
                .build();
    }

    public ReconciliationStatus reconcileEventsByDate(String date, Long minutesForEachTimeFrame) {

        Date startTime = Calendar.getInstance().getTime();

        // Execute checks on date and convert it in required format
        String utcDate = date + "+0000";
        if (!dateValidator.isValid(utcDate)) {
            throw new AppException(AppError.BAD_REQUEST_INVALID_DATE, date);
        }
        String stringedDate = date.replace("-0", "-");

        // Initialize status data
        List<ReconciledEventStatus> coldToHotReconciledEvents = new LinkedList<>();
        List<ReconciledEventStatus> hotToColdReconciledEvents = new LinkedList<>();
        Date dateLowerBound = dateValidator.getDate(utcDate);

        long batchCounter = 1;
        while (!isComputationEnded(date, dateLowerBound)) {

            Date dateUpperBound = dateValidator.getDate(utcDate, minutesForEachTimeFrame * batchCounter);
            Long dateLowerBoundTimestamp = dateLowerBound.getTime() / 1000;
            Long dateUpperBoundTimestamp = dateUpperBound.getTime() / 1000;

            // Retrieving IDs by date either from cold storage and from hot storage
            Set<ConvertedKey> coldStorageIDsForDate = coldStorageRepo.getIDsByDate(stringedDate, dateLowerBoundTimestamp, dateUpperBoundTimestamp).stream()
                    .map(ConvertedKey::new)
                    .collect(Collectors.toSet());
            Set<String> hotStorageIDsForDate = hotStorageRepo.getIDsByDate(CommonUtility.generatePartitionKeyForHotStorage(stringedDate), dateLowerBoundTimestamp, dateUpperBoundTimestamp);
            log.info(String.format("Analyzing time section [%d-%d]. Found [%d] elements in the cold storage and [%d] in the hot storage for the date [%s] (searched as [%s])", dateLowerBoundTimestamp, dateUpperBoundTimestamp, coldStorageIDsForDate.size(), hotStorageIDsForDate.size(), date, stringedDate));

            // Reconcile events from cold storage to hot storage and retrieve the list of status info for each persisted event
            coldToHotReconciledEvents.addAll(reconcileEventsFromColdToHotStorage(coldStorageIDsForDate, hotStorageIDsForDate, stringedDate));

            // Reconcile events from hot storage to cold storage and retrieve the list of status info for each persisted event
            hotToColdReconciledEvents.addAll(reconcileEventsFromHotToColdStorage(coldStorageIDsForDate, hotStorageIDsForDate, stringedDate));

            // Update batch counter and date bounds
            dateLowerBound = dateUpperBound;
            batchCounter++;
        }

        // Last, return the general status for reconciliation operation
        return generateReconciliationStatus(coldToHotReconciledEvents, hotToColdReconciledEvents, startTime, date, stringedDate);
    }

    private List<ReconciledEventStatus> reconcileEventsFromHotToColdStorage(Set<ConvertedKey> coldStorageIDs, Set<String> hotStorageIDs, String stringedDate) {

        // Extract the list of event IDs that are present in hot storage but not present in cold storage
        Set<String> linearizedColdStorageIDs = coldStorageIDs.stream()
                .map(ConvertedKey::getAdaptedKey)
                .collect(Collectors.toSet());
        Set<String> eventIDsNotInColdStorage = hotStorageIDs.stream()
                .filter(id -> !linearizedColdStorageIDs.contains(id))
                .collect(Collectors.toUnmodifiableSet());
        log.info(String.format("Found [%d] elements in the hot storage that are not found in the cold storage.", eventIDsNotInColdStorage.size()));

        // Start reconciliation for each event ID
        List<ReconciledEventStatus> hotToColdReconciledEvents = new LinkedList<>();
        for (String eventID : eventIDsNotInColdStorage) {

            log.info(String.format("Analyzing event with ID [%s] from hot storage.", eventID));

            // Initialize data for reconciled event status object
            ReconciledEventState reconciliationStatus = ReconciledEventState.SUCCESS;
            String errorCause = null;
            String eventInserted = null;

            try {
                // Retrieve the event in hot storage, filtering by event ID
                HotStorageVerifyKO eventInHotStorage = hotStorageRepo.findById(eventID);

                // Extracting raw event from data in hot storage, removing refactored fields in order to produce the same event arrived from Event Hub
                HashMap<String, Object> eventFromEVH = gsonMapper.fromJson(gsonMapper.toJson(eventInHotStorage), HashMap.class);
                String dateTime = CommonUtility.getMapField(eventFromEVH, "faultBean.dateTime", String.class, null);
                CommonUtility.setMapField(eventFromEVH, "pk", null);
                CommonUtility.setMapField(eventFromEVH, "faultBean.timestamp", dateTime);
                CommonUtility.setMapField(eventFromEVH, "faultBean.dateTime", null);
                CommonUtility.setMapField(eventFromEVH, "timeStamp", null);
                String eventToBeSavedInColdBlobStorage = gsonMapper.toJson(eventFromEVH);

                // Convert event stored in cold storage to event to be stored in hot storage
                ColdStorageVerifyKO eventToBeSavedInColdStorage = mapper.map(eventInHotStorage, ColdStorageVerifyKO.class);
                eventToBeSavedInColdStorage.setPartitionKey(stringedDate);

                // Save the raw event in blob storage and set the body reference in the event to be saved
                String blobBodyRef = coldStorageBlobRepo.save(eventToBeSavedInColdBlobStorage, eventToBeSavedInColdStorage.getRowKey());
                eventToBeSavedInColdStorage.setBlobBodyRef(blobBodyRef);

                // Finally, save the event in cold storage and retrieve the row key as event ID
                coldStorageRepo.save(eventToBeSavedInColdStorage);
                eventInserted = eventToBeSavedInColdStorage.getRowKey();

            } catch (Exception e) {

                // Set failure and error cause for reconciled event status object
                reconciliationStatus = ReconciledEventState.FAILURE;
                errorCause = e.getMessage();

            } finally {

                // Generate a new reconciled event status object and insert it in the final list
                hotToColdReconciledEvents.add(ReconciledEventStatus.builder()
                        .eventReconciledFromOtherStorage(eventID)
                        .newEventInserted(eventInserted)
                        .status(reconciliationStatus)
                        .cause(errorCause)
                        .build());
            }
        }

        return hotToColdReconciledEvents;
    }

    private List<ReconciledEventStatus> reconcileEventsFromColdToHotStorage(Set<ConvertedKey> coldStorageIDs, Set<String> hotStorageIDs, String stringedDate) {

        // Extract the list of event IDs that are present in cold storage but not present in hot storage
        Set<ConvertedKey> eventIDsNotInHotStorage = coldStorageIDs.stream()
                .filter(compositeID -> !hotStorageIDs.contains(compositeID.getAdaptedKey()))
                .collect(Collectors.toUnmodifiableSet());
        log.info(String.format("Found [%d] elements in the cold storage that are not found in the hot storage.", eventIDsNotInHotStorage.size()));

        // Start reconciliation for each event ID
        List<ReconciledEventStatus> coldToHotReconciledEvents = new LinkedList<>();
        for (ConvertedKey convertedKey : eventIDsNotInHotStorage) {

            log.info(String.format("Analyzing event with ID [%s] from cold storage.", convertedKey.getRowKey()));

            // Initialize data for reconciled event status object
            ReconciledEventState reconciliationStatus = ReconciledEventState.SUCCESS;
            String errorCause = null;
            String eventInserted = null;

            try {
                // Retrieve the event in cold storage, filtering by row key and partition key, and the raw event in cold blob storage
                String rowKey = convertedKey.getRowKey();
                ColdStorageVerifyKO eventInColdStorage = coldStorageRepo.findById(rowKey, stringedDate);
                Map<String, Object> eventFromBlob = coldStorageBlobRepo.findByID(rowKey);

                // Extracting hot storage event from raw event, adding refactored fields in order to enrich the event arrived from Event Hub
                HotStorageVerifyKO eventToBeSavedInHotStorage = ConvertColdStorageVerifyKOToHotStorageVerifyKO.map(eventFromBlob);
                eventToBeSavedInHotStorage.setPk(CommonUtility.generatePartitionKeyForHotStorage(
                        eventInColdStorage.getPartitionKey()) + "-" +
                        eventToBeSavedInHotStorage.getCreditor().getIdPA() + "-" +
                        eventToBeSavedInHotStorage.getPsp().getIdPsp());
                eventToBeSavedInHotStorage.setServiceIdentifier(CommonUtility.getMapField(eventFromBlob, "serviceIdentifier", String.class, eventInColdStorage.getServiceIdentifier()));
                eventToBeSavedInHotStorage.getFaultBean().setTimestamp(eventInColdStorage.getEventTimestamp());

                // Finally, save the event in cold storage and retrieve the document ID as event ID
                hotStorageRepo.save(eventToBeSavedInHotStorage);
                eventInserted = eventToBeSavedInHotStorage.getId();

            } catch (Exception e) {

                // Set failure and error cause for reconciled event status object
                reconciliationStatus = ReconciledEventState.FAILURE;
                errorCause = e.getMessage();

            } finally {

                // Generate a new reconciled event status object and insert it in the final list
                coldToHotReconciledEvents.add(ReconciledEventStatus.builder()
                        .eventReconciledFromOtherStorage(convertedKey.getRowKey())
                        .newEventInserted(eventInserted)
                        .status(reconciliationStatus)
                        .cause(errorCause)
                        .build());
            }
        }

        return coldToHotReconciledEvents;
    }

    private boolean isComputationEnded(String analyzedDate, Date lowerBoundDate) {
        return !analyzedDate.equals(this.dateValidator.getDateAsString(lowerBoundDate).substring(0, 10));
    }

}
