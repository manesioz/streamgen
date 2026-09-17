package io.streamgen.assign;

import io.streamgen.Assign;

import java.io.Serializable;

public final class EventTime implements Assign<Long>, Serializable {

    private static final long LAG_SEED = 0x45564E54544D4CL;

    private final long startMs;
    private final long stepMs;
    private final long maxLagMs;

    private EventTime(long startMs, long stepMs, long maxLagMs) {
        if (stepMs <= 0) {
            throw new IllegalArgumentException("stepMs must be > 0, got: " + stepMs);
        }
        if (maxLagMs < 0) {
            throw new IllegalArgumentException("maxLagMs must be >= 0, got: " + maxLagMs);
        }
        this.startMs = startMs;
        this.stepMs = stepMs;
        this.maxLagMs = maxLagMs;
    }

    public static EventTime millis(long startMs, long stepMs) {
        return new EventTime(startMs, stepMs, 0);
    }

    public EventTime lag(long maxLagMs) {
        return new EventTime(startMs, stepMs, maxLagMs);
    }

    @Override
    public Long assign(long index) {
        long base = startMs + index * stepMs;
        if (maxLagMs == 0) {
            return base;
        }
        return base - Mix.longInRange(LAG_SEED, index, 0, maxLagMs);
    }
}
