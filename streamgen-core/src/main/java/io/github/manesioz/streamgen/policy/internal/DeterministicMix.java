package io.github.manesioz.streamgen.policy.internal;

/** SplitMix64-style mixing for deterministic, index-addressable pseudo-random values. */
public final class DeterministicMix {

    private DeterministicMix() {}

    public static long mix(long seed, long index) {
        long x = seed ^ (index * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    /** Uniform long in {@code [min, max]} inclusive. */
    public static long longInRange(long seed, long index, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        long span = max - min + 1;
        if (span <= 0) {
            throw new IllegalArgumentException("range too large");
        }
        long mixed = mix(seed, index);
        long unit = mixed >>> 1;
        return min + (unit % span);
    }

    /** Uniform double in {@code [min, max)}. */
    public static double doubleInRange(long seed, long index, double min, double max) {
        if (!(min < max)) {
            throw new IllegalArgumentException("min must be < max");
        }
        long mixed = mix(seed, index);
        double unit = (mixed >>> 11) * 0x1.0p-53;
        return min + unit * (max - min);
    }

    /** True with probability {@code percent / 100}, deterministic in index. */
    public static boolean hitPercent(long seed, long index, int percent) {
        if (percent <= 0) {
            return false;
        }
        if (percent >= 100) {
            return true;
        }
        return (mix(seed, index) >>> 1) % 100 < percent;
    }
}
