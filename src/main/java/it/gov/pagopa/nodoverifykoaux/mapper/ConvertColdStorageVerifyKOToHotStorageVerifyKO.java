package it.gov.pagopa.nodoverifykoaux.mapper;

import it.gov.pagopa.nodoverifykoaux.entity.*;
import it.gov.pagopa.nodoverifykoaux.util.CommonUtility;

import java.util.Map;

public class ConvertColdStorageVerifyKOToHotStorageVerifyKO {

    private ConvertColdStorageVerifyKOToHotStorageVerifyKO() {
    }

    public static HotStorageVerifyKO map(Map<String, Object> eventFromBlob) {
        return HotStorageVerifyKO.builder()
                .id(CommonUtility.getMapField(eventFromBlob, "id", String.class, null))
                .version(CommonUtility.getMapField(eventFromBlob, "version", String.class, null))
                .diagnosticId(CommonUtility.getMapField(eventFromBlob, "diagnosticId", String.class, null))
                .debtorPosition(DebtorData.builder()
                        .modelType(CommonUtility.getMapField(eventFromBlob, "debtorPosition.modelType", String.class, null))
                        .noticeNumber(CommonUtility.getMapField(eventFromBlob, "debtorPosition.noticeNumber", String.class, null))
                        .amount(CommonUtility.getMapField(eventFromBlob, "debtorPosition.amount", Double.class, null))
                        .build())
                .creditor(CreditorData.builder()
                        .idPA(CommonUtility.getMapField(eventFromBlob, "creditor.idPA", String.class, "NA"))
                        .ccPost(CommonUtility.getMapField(eventFromBlob, "creditor.ccPost", String.class, null))
                        .idBrokerPA(CommonUtility.getMapField(eventFromBlob, "creditor.idBrokerPA", String.class, null))
                        .idStation(CommonUtility.getMapField(eventFromBlob, "creditor.idStation", String.class, null))
                        .build())
                .psp(PSPData.builder()
                        .idPsp(CommonUtility.getMapField(eventFromBlob, "psp.idPsp", String.class, "NA"))
                        .idBrokerPsp(CommonUtility.getMapField(eventFromBlob, "psp.idBrokerPsp", String.class, null))
                        .idChannel(CommonUtility.getMapField(eventFromBlob, "psp.idChannel", String.class, null))
                        .build())
                .faultBean(FaultBeanEnrichedData.builder()
                        .faultCode(CommonUtility.getMapField(eventFromBlob, "faultBean.faultCode", String.class, null))
                        .description(CommonUtility.getMapField(eventFromBlob, "faultBean.description", String.class, null))
                        .dateTime(CommonUtility.getMapField(eventFromBlob, "faultBean.timestamp", String.class, null))
                        .build())
                .build();
    }
}
