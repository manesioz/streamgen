package io.github.manesioz.streamgen.policy.time;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.schema.FieldType;

/** Event-time millis: {@code startMs + index * stepMs}. */
public final class MonotonicTimePolicy implements FieldPolicy {

    private final long startMs;
    private final long stepMs;

    public MonotonicTimePolicy(long startMs, long stepMs) {
        if (stepMs <= 0) {
            throw new IllegalArgumentException("stepMs must be > 0, got: " + stepMs);
        }
        this.startMs = startMs;
        this.stepMs = stepMs;
    }

    @Override
    public Object assign(long index) {
        return startMs + (index * stepMs);
    }

    @Override
    public FieldType producedType() {
        return FieldType.BIGINT;
    }
}
