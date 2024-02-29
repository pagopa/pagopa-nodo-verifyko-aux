package it.gov.pagopa.nodoverifykoaux.model;

import lombok.Data;

import java.util.Objects;

@Data
public class ConvertedKey {

    private String adaptedKey;
    private String rowKey;

    public ConvertedKey(String rowKey) {
        this.rowKey = rowKey;
        this.adaptedKey = rowKey.replaceFirst("\\d{8,}-", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConvertedKey that = (ConvertedKey) o;
        return Objects.equals(adaptedKey, that.adaptedKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adaptedKey);
    }
}
