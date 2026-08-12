package io.github.manesioz.streamgen.policy.compose;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.internal.DeterministicMix;
import io.github.manesioz.streamgen.schema.FieldType;

import java.util.Objects;

/**
 * Re-emits a prior index's value for a configured share of indexes. Models duplicate
 * deliveries without mutating generator state.
 *
 * <p>When selected, {@code assign(index)} returns {@code inner.assign(index - lookback)}
 * where lookback is in {@code [1, maxLookback]}. Index 0 always uses the inner policy.
 */
public final class DuplicatePolicy implements FieldPolicy {

    private static final long SEED = 0x4455504C494341L; // "DUPLICA"

    private final FieldPolicy inner;
    private final int percent;
    private final int maxLookback;

    public DuplicatePolicy(FieldPolicy inner, int percent, int maxLookback) {
        this.inner = Objects.requireNonNull(inner, "inner");
        if (percent < 1 || percent > 99) {
            throw new IllegalArgumentException("percent must be in [1, 99], got: " + percent);
        }
        if (maxLookback < 1) {
            throw new IllegalArgumentException("maxLookback must be >= 1, got: " + maxLookback);
        }
        this.percent = percent;
        this.maxLookback = maxLookback;
    }

    public DuplicatePolicy(FieldPolicy inner, int percent) {
        this(inner, percent, 1);
    }

    @Override
    public Object assign(long index) {
        if (index <= 0 || !DeterministicMix.hitPercent(SEED, index, percent)) {
            return inner.assign(index);
        }
        long lookback = 1 + DeterministicMix.longInRange(SEED ^ 1, index, 0, maxLookback - 1);
        long source = Math.max(0, index - lookback);
        return inner.assign(source);
    }

    @Override
    public FieldType producedType() {
        return inner.producedType();
    }
}
