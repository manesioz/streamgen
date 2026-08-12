package io.github.manesioz.streamgen.row;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Ordered name→value record produced by {@link IndexRowGenerator}.
 *
 * <p>Values may be {@code null} when a field's null-rate policy applies.
 */
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
                throw new IllegalArgumentException("field name must be non-blank at index " + i);
            }
            if (map.put(this.names[i], i) != null) {
                throw new IllegalArgumentException("duplicate field name: " + this.names[i]);
            }
        }
        this.indexByName = Collections.unmodifiableMap(map);
    }

    public int size() {
        return names.length;
    }

    public String[] names() {
        return Arrays.copyOf(names, names.length);
    }

    public Object get(int index) {
        return values[index];
    }

    public Object get(String name) {
        Integer index = indexByName.get(name);
        if (index == null) {
            throw new IllegalArgumentException("unknown field: " + name);
        }
        return values[index];
    }

    public String getString(String name) {
        Object value = get(name);
        return value == null ? null : (String) value;
    }

    public long getLong(String name) {
        Object value = get(name);
        if (value == null) {
            throw new NullPointerException("field '" + name + "' is null");
        }
        return (Long) value;
    }

    public double getDouble(String name) {
        Object value = get(name);
        if (value == null) {
            throw new NullPointerException("field '" + name + "' is null");
        }
        return (Double) value;
    }

    public boolean getBoolean(String name) {
        Object value = get(name);
        if (value == null) {
            throw new NullPointerException("field '" + name + "' is null");
        }
        return (Boolean) value;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return "Row" + asMap();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Row)) {
            return false;
        }
        Row row = (Row) o;
        return Arrays.equals(names, row.names) && Arrays.equals(values, row.values);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(names) + Arrays.hashCode(values);
    }
}
