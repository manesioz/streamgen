package io.streamgen.assign;

import io.streamgen.Assign;

public final class Wrap {

    private static final long NULL_SEED = 0x4E554C4C5300L;
    private static final long AGAIN_SEED = 0x414741494E00L;

    private Wrap() {}

    public static <V> Assign<V> nulls(double rate, Assign<V> inner) {
        if (inner == null) {
            throw new IllegalArgumentException("inner must not be null");
        }
        if (rate < 0 || rate > 1 || Double.isNaN(rate)) {
            throw new IllegalArgumentException("rate must be in [0, 1], got: " + rate);
        }
        return index -> Mix.hit(NULL_SEED, index, rate) ? null : inner.assign(index);
    }

    public static <V> Assign<V> again(double rate, Assign<V> inner) {
        if (inner == null) {
            throw new IllegalArgumentException("inner must not be null");
        }
        if (rate < 0 || rate > 1 || Double.isNaN(rate)) {
            throw new IllegalArgumentException("rate must be in [0, 1], got: " + rate);
        }
        return index -> {
            if (index <= 0 || !Mix.hit(AGAIN_SEED, index, rate)) {
                return inner.assign(index);
            }
            return inner.assign(index - 1);
        };
    }
}
