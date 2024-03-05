package it.gov.pagopa.nodoverifykoaux.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@Slf4j
public class DateValidator {

    private final SimpleDateFormat dateFormatter;

    public DateValidator(String format) {
        this.dateFormatter = new SimpleDateFormat(format);
        this.dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public boolean isValid(String date) {
        return getDate(date) != null;
    }

    public boolean isValid(Integer year, Integer month) {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        return (year == currentYear && month < currentMonth) || year < currentYear;
    }

    public Date getDate(String dateAsString) {
        Date date = null;
        dateFormatter.setLenient(false);
        try {
            date = dateFormatter.parse(dateAsString);
        } catch (ParseException e) {
            log.warn(String.format("Error while trying to parse string as date. Invalid string format: [%s] must follows 'yyyy-MM-dd' format", dateAsString));
        }
        return date;
    }

    public Date getDate(String dateAsString, Long minutesFromStartDay) {
        Date date = null;
        dateFormatter.setLenient(false);
        try {
            date = dateFormatter.parse(dateAsString);
            date.setTime(date.getTime() + (minutesFromStartDay * 60 * 1000));
        } catch (ParseException e) {
            log.warn(String.format("Error while trying to parse string as date. Invalid string format: [%s] must follows 'yyyy-MM-dd' format", dateAsString));
        }
        return date;
    }

    public List<String> getDaysOfMonth(Integer year, Integer month) {
        List<String> days = new LinkedList<>();
        YearMonth yearMonth = YearMonth.of(year, Month.of(month));
        yearMonth.atDay(1)
                .datesUntil(yearMonth.plusMonths(1).atDay(1))
                .forEach(day -> days.add(day.toString() + "+0000"));
        return days;
    }

    public String getDateFromTimestamp(Long timestamp) {
        return LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC")).toString();
    }

    public String getDateAsString(Date date) {
        return dateFormatter.format(date);
    }
}
