package it.gov.pagopa.nodoverifykoaux.model.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReconciliationStatistics implements Serializable {
    private Date startedAt;
    private Date endedAt;
    private ReconciliationHotColdComparation analyzed;
    private ReconciliationHotColdComparation succeeded;
    private ReconciliationHotColdComparation failed;
    private ReconciliationHotColdComparation toReconcile;
}
