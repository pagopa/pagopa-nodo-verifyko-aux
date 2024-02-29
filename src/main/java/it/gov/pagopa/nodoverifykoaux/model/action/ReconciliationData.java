package it.gov.pagopa.nodoverifykoaux.model.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReconciliationData implements Serializable {
    private String date;
    private String usedDateForSearch;
    private List<ReconciledEventStatus> fromHotToColdStorage;
    private List<ReconciledEventStatus> fromColdToHotStorage;
}
