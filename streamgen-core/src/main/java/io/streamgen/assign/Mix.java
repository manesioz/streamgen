package io.streamgen.assign;

final class Mix {

    private Mix() {}

    static long mix(long seed, long index) {
        long x = seed ^ (index * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    static long longInRange(long seed, long index, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        long span = max - min + 1;
        if (span <= 0) {
            throw new IllegalArgumentException("range too large");
        }
        long unit = mix(seed, index) >>> 1;
        return min + (unit % span);
    }

    static double doubleInRange(long seed, long index, double min, double max) {
        if (!(min < max)) {
            throw new IllegalArgumentException("min must be < max");
        }
        double unit = (mix(seed, index) >>> 11) * 0x1.0p-53;
        return min + unit * (max - min);
    }

    static boolean hit(long seed, long index, double rate) {
        if (rate <= 0) {
            return false;
        }
        if (rate >= 1) {
            return true;
        }
        return (mix(seed, index) >>> 1) % 1000 < Math.round(rate * 1000);
    }
}
