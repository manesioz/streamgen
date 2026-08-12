package io.streamgen;

public final class Multiplicity {

    private Multiplicity() {}

    public static <T> Assign<T> duplicates(Assign<T> rows, long everyN) {
        if (everyN <= 1) {
            throw new IllegalArgumentException("everyN must be > 1");
        }
        return index -> rows.assign(index > 0 && index % everyN == 0 ? index - 1 : index);
    }
}
