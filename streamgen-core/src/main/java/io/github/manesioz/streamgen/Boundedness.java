package io.github.manesioz.streamgen;

import java.io.Serializable;

/** How many indexes a generator should emit. Lives in core so tests can materialize without Flink. */
public final class Boundedness implements Serializable {

    private final long count;

    private Boundedness(long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0, got: " + count);
        }
        this.count = count;
    }

    public static Boundedness finite(long count) {
        return new Boundedness(count);
    }

    public static Boundedness unbounded() {
        return new Boundedness(Long.MAX_VALUE);
    }

    public boolean isBounded() {
        return count < Long.MAX_VALUE;
    }

    public long count() {
        return count;
    }
}
