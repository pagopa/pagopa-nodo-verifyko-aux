package it.gov.pagopa.nodoverifykoaux.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebtorData {
    private String modelType;
    private String noticeNumber;
    private Double amount;
}
