package io.github.manesioz.streamgen.policy.compose;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.internal.DeterministicMix;
import io.github.manesioz.streamgen.schema.FieldType;

import java.util.Objects;

/**
 * Weighted mix of two policies of the same produced type. Choice is a pure function of index.
 */
public final class MixPolicy implements FieldPolicy {

    private static final long SEED = 0x4D49584C4F4E47L; // "MIXLONG"

    private final FieldPolicy left;
    private final FieldPolicy right;
    private final int leftPercent;

    public MixPolicy(FieldPolicy left, int leftWeight, FieldPolicy right, int rightWeight) {
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
        if (left.producedType() != right.producedType()) {
            throw new IllegalArgumentException(
                    "mixed policies must produce the same type, got "
                            + left.producedType()
                            + " and "
                            + right.producedType());
        }
        if (leftWeight <= 0 || rightWeight <= 0) {
            throw new IllegalArgumentException("weights must be > 0");
        }
        int total = leftWeight + rightWeight;
        this.leftPercent = (int) Math.round(100.0 * leftWeight / total);
        if (this.leftPercent <= 0 || this.leftPercent >= 100) {
            throw new IllegalArgumentException("weights must not collapse to a single policy");
        }
    }

    @Override
    public Object assign(long index) {
        if (DeterministicMix.hitPercent(SEED, index, leftPercent)) {
            return left.assign(index);
        }
        return right.assign(index);
    }

    @Override
    public FieldType producedType() {
        return left.producedType();
    }
}
