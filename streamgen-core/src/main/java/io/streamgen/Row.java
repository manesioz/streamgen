package io.streamgen;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Row implements Serializable {

    private final String[] names;
    private final Object[] values;
    private final Map<String, Integer> indexByName;

    public Row(String[] names, Object[] values) {
        Objects.requireNonNull(names, "names");
        Objects.requireNonNull(values, "values");
        if (names.length != values.length) {
            throw new IllegalArgumentException("names and values length mismatch");
        }
        this.names = Arrays.copyOf(names, names.length);
        this.values = Arrays.copyOf(values, values.length);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < this.names.length; i++) {
            if (this.names[i] == null || this.names[i].isBlank()) {
                throw new IllegalArgumentException("column name must be non-blank at index " + i);
            }
            if (map.put(this.names[i], i) != null) {
                throw new IllegalArgumentException("duplicate column: " + this.names[i]);
            }
        }
        this.indexByName = Collections.unmodifiableMap(map);
    }

    public Object get(String name) {
        Integer index = indexByName.get(name);
        if (index == null) {
            throw new IllegalArgumentException("unknown column: " + name);
        }
        return values[index];
    }

    public <T> T get(String name, Class<T> type) {
        Object value = get(name);
        return value == null ? null : type.cast(value);
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Row row)) {
            return false;
        }
        return Arrays.equals(names, row.names) && Arrays.equals(values, row.values);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(names) + Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "Row" + asMap();
    }
}
