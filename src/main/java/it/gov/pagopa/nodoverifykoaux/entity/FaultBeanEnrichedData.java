package it.gov.pagopa.nodoverifykoaux.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaultBeanEnrichedData {
    private String faultCode;
    private String description;
    private Long timestamp;
    private String dateTime;
}
