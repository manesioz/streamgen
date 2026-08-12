package io.github.manesioz.streamgen.policy;

import io.github.manesioz.streamgen.policy.compose.DuplicatePolicy;
import io.github.manesioz.streamgen.policy.compose.JitterPolicy;
import io.github.manesioz.streamgen.policy.compose.MixPolicy;

/** Decorators. Compose instead of adding knobs to every policy. */
public final class Policies {

    private Policies() {}

    public static FieldPolicy mix(FieldPolicy left, int leftWeight, FieldPolicy right, int rightWeight) {
        return new MixPolicy(left, leftWeight, right, rightWeight);
    }

    /** Re-emit a prior value for {@code percent} of indexes (lookback 1). */
    public static FieldPolicy duplicate(FieldPolicy inner, int percent) {
        return new DuplicatePolicy(inner, percent);
    }

    public static FieldPolicy duplicate(FieldPolicy inner, int percent, int maxLookback) {
        return new DuplicatePolicy(inner, percent, maxLookback);
    }

    /** Subtract a deterministic lag in {@code [0, maxLagMs]} from a BIGINT policy. */
    public static FieldPolicy jitter(FieldPolicy inner, long maxLagMs) {
        return new JitterPolicy(inner, maxLagMs);
    }
}
