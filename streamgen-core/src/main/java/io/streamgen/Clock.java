package io.streamgen;

public final class Clock {

    private Clock() {}

    public static Assign<Long> stepping(long startMs, long stepMs) {
        if (stepMs < 0) {
            throw new IllegalArgumentException("stepMs must be >= 0");
        }
        return index -> startMs + index * stepMs;
    }

    public static Assign<Long> outOfOrder(Assign<Long> clock, long maxLagMs) {
        if (maxLagMs < 0) {
            throw new IllegalArgumentException("maxLagMs must be >= 0");
        }
        return index -> clock.assign(index) - Math.floorMod(index * 31, maxLagMs + 1);
    }

    public static Assign<Long> late(Assign<Long> clock, long everyN, long lateByMs) {
        if (everyN <= 0) {
            throw new IllegalArgumentException("everyN must be > 0");
        }
        if (lateByMs < 0) {
            throw new IllegalArgumentException("lateByMs must be >= 0");
        }
        return index -> {
            long ts = clock.assign(index);
            return index > 0 && index % everyN == 0 ? ts - lateByMs : ts;
        };
    }
}
