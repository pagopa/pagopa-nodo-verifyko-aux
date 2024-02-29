package it.gov.pagopa.nodoverifykoaux.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;
import lombok.*;
import org.springframework.data.annotation.Id;

@Container(containerName = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotStorageVerifyKO extends TableServiceEntity {

    @Id
    private String id;

    @PartitionKey
    @JsonProperty("PartitionKey")
    private String pk;

    private String version;

    private String diagnosticId;

    private String serviceIdentifier;

    private DebtorData debtorPosition;

    private CreditorData creditor;

    private PSPData psp;

    private FaultBeanEnrichedData faultBean;
}
