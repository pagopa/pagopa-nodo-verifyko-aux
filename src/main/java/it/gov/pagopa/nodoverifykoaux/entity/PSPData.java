package it.gov.pagopa.nodoverifykoaux.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSPData {
    private String idPsp;
    private String idBrokerPsp;
    private String idChannel;
}
