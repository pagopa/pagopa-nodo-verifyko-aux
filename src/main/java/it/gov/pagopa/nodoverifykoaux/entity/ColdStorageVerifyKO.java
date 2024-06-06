package it.gov.pagopa.nodoverifykoaux.entity;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ColdStorageVerifyKO extends TableServiceEntity {

    private String blobBodyRef;
    private String dateTime;
    private String idChannel;
    private String idPA;
    private String idPsp;
    private String idStation;
    private String noticeNumber;
    private String serviceIdentifier;
    private String diagnosticId;
    private Long eventTimestamp;

    @Override
    public void readEntity(final HashMap<String, EntityProperty> properties, final OperationContext opContext) throws StorageException {
        super.readEntity(properties, opContext);
        this.blobBodyRef = Optional.ofNullable(properties.get("blobBodyRef")).map(EntityProperty::getValueAsString).orElse(blobBodyRef);
        this.dateTime = Optional.ofNullable(properties.get("dateTime")).map(EntityProperty::getValueAsString).orElse(dateTime);
        this.idChannel = Optional.ofNullable(properties.get("idChannel")).map(EntityProperty::getValueAsString).orElse(idChannel);
        this.idPA = Optional.ofNullable(properties.get("idPA")).map(EntityProperty::getValueAsString).orElse(idPA);
        this.idPsp = Optional.ofNullable(properties.get("idPsp")).map(EntityProperty::getValueAsString).orElse(idPsp);
        this.idStation = Optional.ofNullable(properties.get("idStation")).map(EntityProperty::getValueAsString).orElse(idStation);
        this.noticeNumber = Optional.ofNullable(properties.get("noticeNumber")).map(EntityProperty::getValueAsString).orElse(noticeNumber);
        this.serviceIdentifier = Optional.ofNullable(properties.get("serviceIdentifier")).map(EntityProperty::getValueAsString).orElse(serviceIdentifier);
        this.diagnosticId = Optional.ofNullable(properties.get("diagnosticId")).map(EntityProperty::getValueAsString).orElse(diagnosticId);
        this.eventTimestamp = Optional.ofNullable(properties.get("timestamp")).map(EntityProperty::getValueAsLong).orElse(eventTimestamp);
    }

    @Override
    public HashMap<String, EntityProperty> writeEntity(OperationContext operationContext) throws StorageException {
        super.writeEntity(operationContext);

        HashMap<String, EntityProperty> properties = new HashMap<>();
        properties.put("blobBodyRef", new EntityProperty(this.blobBodyRef));
        properties.put("dateTime", new EntityProperty(this.dateTime));
        properties.put("idChannel", new EntityProperty(this.idChannel));
        properties.put("idPA", new EntityProperty(this.idPA));
        properties.put("idPsp", new EntityProperty(this.idPsp));
        properties.put("idStation", new EntityProperty(this.idStation));
        properties.put("noticeNumber", new EntityProperty(this.noticeNumber));
        properties.put("serviceIdentifier", new EntityProperty(this.serviceIdentifier));
        properties.put("diagnosticId", new EntityProperty(this.diagnosticId));
        properties.put("timestamp", new EntityProperty(this.eventTimestamp));

        return properties;
    }
}
