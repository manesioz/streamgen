package io.streamgen;

public final class Values {

    private Values() {}

    public static Assign<Long> index() {
        return index -> index;
    }

    public static Assign<String> prefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }
        return index -> prefix + index;
    }

    public static Assign<Double> range(double min, double max) {
        if (!(min < max)) {
            throw new IllegalArgumentException("min must be < max");
        }
        return index -> min + (Math.floorMod(index, 10_000) / 10_000.0) * (max - min);
    }
}
