package it.gov.pagopa.nodoverifykoaux.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditorData {
    private String idPA;
    private String ccPost;
    private String idBrokerPA;
    private String idStation;
}
