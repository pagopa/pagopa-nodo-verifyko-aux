package it.gov.pagopa.nodoverifykoaux.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtility {

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(Object value) {
        return Optional.ofNullable(value).orElse("").toString();
    }

    /**
     * @param value value to deNullify.
     * @return return false if value is null
     */
    public static Boolean deNull(Boolean value) {
        return Optional.ofNullable(value).orElse(false);
    }

    @SuppressWarnings({"rawtypes"})
    public static <T> T getMapField(Map<String, Object> map, String name, Class<T> clazz, T defaultValue) {
        T field = null;
        List<String> splitPath = List.of(name.split("\\."));
        Map eventSubset = map;
        Iterator<String> it = splitPath.listIterator();
        while (it.hasNext()) {
            Object retrievedEventField = eventSubset.get(it.next());
            if (!it.hasNext()) {
                field = clazz.cast(retrievedEventField);
            } else {
                eventSubset = (Map) retrievedEventField;
                if (eventSubset == null) {
                    throw new IllegalArgumentException("The field [" + name + "] does not exists in the passed map.");
                }
            }
        }
        return field == null ? defaultValue : field;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void setMapField(Map<String, Object> map, String name, T value) {
        List<String> splitPath = List.of(name.split("\\."));
        Map eventSubset = map;
        Iterator<String> it = splitPath.listIterator();
        while (it.hasNext()) {
            String field = it.next();
            Object retrievedEventField = eventSubset.get(field);
            if (!it.hasNext()) {
                eventSubset.put(field, value);
            } else {
                eventSubset = (Map) retrievedEventField;
                if (eventSubset == null) {
                    throw new IllegalArgumentException("The field [" + name + "] does not exists in the passed map.");
                }
            }
        }
    }

    public static String generatePartitionKeyForHotStorage(String date) {
        return date.replace("-0", "-").replace("-", "");
    }
}
