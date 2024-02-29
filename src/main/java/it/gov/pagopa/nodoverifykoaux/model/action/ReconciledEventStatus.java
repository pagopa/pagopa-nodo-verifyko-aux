package it.gov.pagopa.nodoverifykoaux.model.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReconciledEventStatus implements Serializable {
    private String eventReconciledFromOtherStorage;
    private String newEventInserted;
    private ReconciledEventState status;
    private String cause;
}
