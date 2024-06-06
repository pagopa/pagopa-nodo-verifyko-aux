package it.gov.pagopa.nodoverifykoaux.service;

import it.gov.pagopa.nodoverifykoaux.entity.ColdStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.entity.HotStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.exception.AppError;
import it.gov.pagopa.nodoverifykoaux.exception.AppException;
import it.gov.pagopa.nodoverifykoaux.model.statistics.DailyDataReport;
import it.gov.pagopa.nodoverifykoaux.model.statistics.DataReport;
import it.gov.pagopa.nodoverifykoaux.repository.DataStorageRepository;
import it.gov.pagopa.nodoverifykoaux.repository.TableStorageRepository;
import it.gov.pagopa.nodoverifykoaux.util.DateValidator;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatisticsService {

    private final DataStorageRepository hotStorageRepo;

    private final TableStorageRepository coldStorageRepo;

    private final DateValidator dateValidator;

    private final ModelMapper mapper;

    private final int reportMinutesBatchSize;

    public StatisticsService(DataStorageRepository hotStorageRepo,
                             TableStorageRepository coldStorageRepo,
                             ModelMapper mapper,
                             @Value("${verifyko.report.minutes-time-frame}") Integer reportMinutesTimeFrame) {
        this.hotStorageRepo = hotStorageRepo;
        this.coldStorageRepo = coldStorageRepo;
        this.dateValidator = new DateValidator("yyyy-MM-ddZ");
        this.mapper = mapper;
        this.reportMinutesBatchSize = reportMinutesTimeFrame;
    }

    public DataReport extractReportFromHotStorage(Integer year, Integer month, Integer singleDay) {

        // Execute checks on date and convert it in required format
        if (!dateValidator.isValid(year, month, singleDay)) {
            throw new AppException(AppError.BAD_REQUEST_INVALID_DATE_FOR_REPORT, year, month, singleDay == null ? "XX" : singleDay);
        }

        DataReport dataReport = new DataReport();

        List<String> days = dateValidator.getDaysOfMonth(year, month, singleDay, true);
        for (String day : days) {

            log.info(String.format("Extracting report data from day [%s].", day));
            DailyDataReport dailyDataReport = new DailyDataReport(day);

            String stringedDate = day.replace("-0", "").replace("-", "").replace("+0000", "");

            Date dateLowerBound = dateValidator.getDate(day);
            long batchCounter = 1;
            while (!isComputationEnded(day, dateLowerBound)) {

                Date dateUpperBound = dateValidator.getDate(day, this.reportMinutesBatchSize * batchCounter);
                Long dateLowerBoundTimestamp = dateLowerBound.getTime() / 1000;
                Long dateUpperBoundTimestamp = dateUpperBound.getTime() / 1000;

                // Retrieving all events batched by defined timestamp section
                List<HotStorageVerifyKO> events = hotStorageRepo.getByDate(stringedDate, dateLowerBoundTimestamp, dateUpperBoundTimestamp);

                // Incrementing statistics counters
                events.forEach(event -> dailyDataReport.addFault(event.getFaultBean().getFaultCode()));

                // Update batch counter and date bounds
                dateLowerBound = dateUpperBound;
                batchCounter++;
            }

            dataReport.addDailyReport(dailyDataReport);
        }

        for (DailyDataReport dayReport : dataReport.getDays()) {
            long counter = 0;
            for (Map.Entry<String, Long> entry : dayReport.getFault().entrySet()) {
                long number = entry.getValue();
                dataReport.addStatus(entry.getKey(), number);
                counter += number;
            }
            dataReport.addToTotal(counter);
        }

        return dataReport;
    }


    public DataReport extractReportFromColdStorage(Integer year, Integer month, Integer singleDay) {

        // Execute checks on date and convert it in required format
        if (!dateValidator.isValid(year, month, singleDay)) {
            throw new AppException(AppError.BAD_REQUEST_INVALID_DATE_FOR_REPORT, year, month, singleDay == null ? "XX" : singleDay);
        }

        DataReport dataReport = new DataReport();

        List<String> days = dateValidator.getDaysOfMonth(year, month, singleDay, false);
        for (String day : days) {

            log.info(String.format("Extracting report data from day [%s].", day));
            DailyDataReport dailyDataReport = new DailyDataReport(day);

            String stringedDate = day.replace("-0", "-");

            Date dateLowerBound = dateValidator.getDate(day + "+0000");
            long batchCounter = 1;
            while (!isComputationEnded(day, dateLowerBound)) {

                Date dateUpperBound = dateValidator.getDate(day + "+0000", this.reportMinutesBatchSize * batchCounter);
                Long dateLowerBoundTimestamp = dateLowerBound.getTime() / 1000;
                Long dateUpperBoundTimestamp = dateUpperBound.getTime() / 1000;

                // Retrieving all events batched by defined timestamp section
                List<ColdStorageVerifyKO> events = coldStorageRepo.getByDate(stringedDate, dateLowerBoundTimestamp, dateUpperBoundTimestamp);

                // Incrementing statistics counters
                events.forEach(event -> dailyDataReport.addFault("RECONCILED"));

                // Update batch counter and date bounds
                dateLowerBound = dateUpperBound;
                batchCounter++;
            }

            dataReport.addDailyReport(dailyDataReport);
        }

        for (DailyDataReport dayReport : dataReport.getDays()) {
            long counter = 0;
            for (Map.Entry<String, Long> entry : dayReport.getFault().entrySet()) {
                long number = entry.getValue();
                dataReport.addStatus(entry.getKey(), number);
                counter += number;
            }
            dataReport.addToTotal(counter);
        }

        return dataReport;
    }


    private boolean isComputationEnded(String analyzedDate, Date lowerBoundDate) {
        return !analyzedDate.substring(0, 10).equals(this.dateValidator.getDateAsString(lowerBoundDate).substring(0, 10));
    }
}
