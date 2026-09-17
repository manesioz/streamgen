package io.streamgen.assign;

import io.streamgen.Assign;

import java.util.List;

public final class Strings {

    private Strings() {}

    public static Assign<String> prefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }
        return index -> prefix + index;
    }

    public static Assign<String> oneOf(String... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("oneOf requires at least one value");
        }
        List<String> copy = List.of(values);
        return index -> copy.get((int) (index % copy.size()));
    }
}
