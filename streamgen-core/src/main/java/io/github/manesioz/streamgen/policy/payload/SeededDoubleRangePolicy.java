package io.github.manesioz.streamgen.policy.payload;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.internal.DeterministicMix;
import io.github.manesioz.streamgen.schema.FieldType;

/** Deterministic double in {@code [min, max)} via seed+index mixing. */
public final class SeededDoubleRangePolicy implements FieldPolicy {

    private final long seed;
    private final double min;
    private final double max;

    public SeededDoubleRangePolicy(long seed, double min, double max) {
        if (!(min < max)) {
            throw new IllegalArgumentException("min must be < max");
        }
        this.seed = seed;
        this.min = min;
        this.max = max;
    }

    @Override
    public Object assign(long index) {
        return DeterministicMix.doubleInRange(seed, index, min, max);
    }

    @Override
    public FieldType producedType() {
        return FieldType.DOUBLE;
    }
}
