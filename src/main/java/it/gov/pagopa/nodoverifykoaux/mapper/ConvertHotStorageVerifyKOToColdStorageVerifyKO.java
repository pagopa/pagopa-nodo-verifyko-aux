package it.gov.pagopa.nodoverifykoaux.mapper;

import it.gov.pagopa.nodoverifykoaux.entity.ColdStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.entity.HotStorageVerifyKO;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ConvertHotStorageVerifyKOToColdStorageVerifyKO implements Converter<HotStorageVerifyKO, ColdStorageVerifyKO> {

    @Override
    public ColdStorageVerifyKO convert(MappingContext<HotStorageVerifyKO, ColdStorageVerifyKO> context) {
        HotStorageVerifyKO src = context.getSource();


        ColdStorageVerifyKO result = ColdStorageVerifyKO.builder()
                .blobBodyRef(null) // this field must be manually set because its value is dynamically defined after persistence in Blob storage
                .dateTime(src.getFaultBean().getDateTime())
                .idChannel(src.getPsp().getIdChannel())
                .idPA(src.getCreditor().getIdPA())
                .idPsp(src.getPsp().getIdPsp())
                .idStation(src.getCreditor().getIdStation())
                .noticeNumber(src.getDebtorPosition().getNoticeNumber())
                .serviceIdentifier(src.getServiceIdentifier())
                .diagnosticId(src.getDiagnosticId())
                .eventTimestamp(src.getFaultBean().getTimestamp())
                .build();


        result.setPartitionKey(null); // this field must be manually set because its value cannot be correctly retrieved from hot-storage event
        result.setRowKey(src.getFaultBean().getTimestamp() + "-" + src.getId());
        return result;
    }
}
