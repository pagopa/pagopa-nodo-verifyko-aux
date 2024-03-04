package it.gov.pagopa.nodoverifykoaux.model.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyDataReport implements Serializable {
    private String day;
    private Map<String, Long> fault;

    public DailyDataReport(String day) {
        this.day = day.substring(0, 10);
        this.fault = new HashMap<>();
    }

    public void addFault(String fault) {
        this.fault.put(fault, this.fault.getOrDefault(fault, 0L) + 1);
    }
}
