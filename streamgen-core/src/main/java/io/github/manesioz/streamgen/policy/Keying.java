package io.github.manesioz.streamgen.policy;

import io.github.manesioz.streamgen.policy.keying.HotKeyPolicy;
import io.github.manesioz.streamgen.policy.keying.UniformKeyPolicy;

/** Factories for key-cardinality field policies. */
public final class Keying {

    private Keying() {}

    public static FieldPolicy uniform(int numKeys) {
        return new UniformKeyPolicy(numKeys);
    }

    public static FieldPolicy hot(int numKeys, int hotPercent) {
        return new HotKeyPolicy(numKeys, hotPercent);
    }

    public static FieldPolicy hot(int numKeys, int hotKeyIndex, int hotPercent) {
        return new HotKeyPolicy(numKeys, hotKeyIndex, hotPercent);
    }

    /** {@code hotFraction} in {@code (0, 1)}. */
    public static FieldPolicy hot(int numKeys, double hotFraction) {
        if (hotFraction <= 0.0 || hotFraction >= 1.0 || Double.isNaN(hotFraction)) {
            throw new IllegalArgumentException("hotFraction must be in (0, 1), got: " + hotFraction);
        }
        int percent = (int) Math.round(hotFraction * 100.0);
        percent = Math.min(99, Math.max(1, percent));
        return new HotKeyPolicy(numKeys, percent);
    }
}
