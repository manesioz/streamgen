package io.github.manesioz.streamgen.policy.compose;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.internal.DeterministicMix;
import io.github.manesioz.streamgen.schema.FieldType;

import java.util.Objects;

/**
 * Subtracts a deterministic lag in {@code [0, maxLagMs]} from a BIGINT inner value.
 * Models late event-time without wall-clock or watermarks (jobs own watermarks).
 */
public final class JitterPolicy implements FieldPolicy {

    private static final long SEED = 0x4A4954544552L; // "JITTER"

    private final FieldPolicy inner;
    private final long maxLagMs;

    public JitterPolicy(FieldPolicy inner, long maxLagMs) {
        this.inner = Objects.requireNonNull(inner, "inner");
        if (inner.producedType() != FieldType.BIGINT) {
            throw new IllegalArgumentException(
                    "jitter requires BIGINT inner policy, got " + inner.producedType());
        }
        if (maxLagMs < 0) {
            throw new IllegalArgumentException("maxLagMs must be >= 0, got: " + maxLagMs);
        }
        this.maxLagMs = maxLagMs;
    }

    @Override
    public Object assign(long index) {
        long base = (Long) inner.assign(index);
        if (maxLagMs == 0) {
            return base;
        }
        long lag = DeterministicMix.longInRange(SEED, index, 0, maxLagMs);
        return base - lag;
    }

    @Override
    public FieldType producedType() {
        return FieldType.BIGINT;
    }
}
