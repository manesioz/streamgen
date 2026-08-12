package io.github.manesioz.streamgen.policy.payload;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.internal.DeterministicMix;
import io.github.manesioz.streamgen.schema.FieldType;

/** Deterministic long in {@code [min, max]} via seed+index mixing. */
public final class SeededLongRangePolicy implements FieldPolicy {

    private final long seed;
    private final long min;
    private final long max;

    public SeededLongRangePolicy(long seed, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.seed = seed;
        this.min = min;
        this.max = max;
    }

    @Override
    public Object assign(long index) {
        return DeterministicMix.longInRange(seed, index, min, max);
    }

    @Override
    public FieldType producedType() {
        return FieldType.BIGINT;
    }
}
