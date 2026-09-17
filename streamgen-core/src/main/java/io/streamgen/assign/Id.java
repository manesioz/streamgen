package io.streamgen.assign;

import io.streamgen.Assign;

public final class Id {

    private Id() {}

    public static Assign<String> even(int n) {
        return even(n, "");
    }

    public static Assign<String> even(int n, String prefix) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0, got: " + n);
        }
        String p = prefix == null ? "" : prefix;
        return index -> p + (index % n);
    }

    public static Assign<String> hot(int n, int percent) {
        return hot(n, percent, "");
    }

    public static Assign<String> hot(int n, int percent, String prefix) {
        if (n < 2) {
            throw new IllegalArgumentException("n must be >= 2 for hot keys, got: " + n);
        }
        if (percent < 1 || percent > 99) {
            throw new IllegalArgumentException("percent must be in [1, 99], got: " + percent);
        }
        String p = prefix == null ? "" : prefix;
        return index -> {
            if ((index % 100) < percent) {
                return p + 0;
            }
            int coldCount = n - 1;
            int coldOffset = (int) (index % coldCount);
            int keyIndex = coldOffset + 1;
            return p + keyIndex;
        };
    }
}
