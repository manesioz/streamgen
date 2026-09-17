package io.streamgen.assign;

import io.streamgen.Assign;

public final class Numbers {

    private static final long LONG_SEED = 0x4E554D4C4F4E47L;
    private static final long DOUBLE_SEED = 0x4E554D44424C45L;

    private Numbers() {}

    public static Assign<Long> index() {
        return index -> index;
    }

    public static Assign<Integer> indexInt() {
        return index -> (int) index;
    }

    public static Assign<Long> between(long min, long max) {
        return index -> Mix.longInRange(LONG_SEED, index, min, max);
    }

    public static Assign<Double> between(double min, double max) {
        return index -> Mix.doubleInRange(DOUBLE_SEED, index, min, max);
    }
}
