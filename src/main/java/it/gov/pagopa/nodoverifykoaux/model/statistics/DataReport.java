package it.gov.pagopa.nodoverifykoaux.model.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataReport implements Serializable {
    List<DailyDataReport> days;
    private long total;
    private Map<String, Long> status;

    public DataReport() {
        this.status = new HashMap<>();
        this.days = new LinkedList<>();
    }

    public void addDailyReport(DailyDataReport dailyDataReport) {
        this.days.add(dailyDataReport);
    }

    public void addStatus(String status, Long value) {
        this.status.put(status, this.status.getOrDefault(status, 0L) + value);
    }

    public void addToTotal(long value) {
        this.total += value;
    }
}
