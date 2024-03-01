package it.gov.pagopa.nodoverifykoaux.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
public class DateValidator {

    private final SimpleDateFormat dateFormatter;

    public DateValidator(String format) {
        this.dateFormatter = new SimpleDateFormat(format);
    }

    public boolean isValid(String date) {
        return getDate(date) != null;
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

    public Long getDateAsTimestamp(String dateAsString) {
        return ZonedDateTime.parse(dateAsString).toEpochSecond();
    }

    public String getDateAsString(Date date) {
        return dateFormatter.format(date);
    }
}
