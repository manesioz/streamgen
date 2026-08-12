package io.streamgen;

import java.io.Serializable;
import java.util.List;

public interface Distribution extends Serializable {

    long slot(long index);

    long size();

    static Distribution uniform(long size) {
        return new Uniform(size);
    }

    static Distribution hot(long size, int hotPercent) {
        return new Hot(size, hotPercent);
    }

    default <V> Assign<V> of(Assign<V> value) {
        return index -> value.assign(slot(index));
    }

    default Assign<String> prefixed(String prefix) {
        return index -> prefix + slot(index);
    }

    default <V> Assign<V> pick(List<V> values) {
        if (values.size() != size()) {
            throw new IllegalArgumentException(
                    "expected " + size() + " values for this distribution, got " + values.size());
        }
        List<V> copy = List.copyOf(values);
        return index -> copy.get((int) slot(index));
    }
}
