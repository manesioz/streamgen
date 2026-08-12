package io.github.manesioz.streamgen.validity;

import java.io.Serializable;

/**
 * Index-derived null probability for payload fields.
 *
 * <p>{@code percent} in {@code [0, 100]}: roughly that percentage of indexes yield null via
 * {@code (index % 100) < percent}. Deterministic and replayable.
 */
public final class NullRate implements Serializable {

    private final int percent;

    private NullRate(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be in [0, 100], got: " + percent);
        }
        this.percent = percent;
    }

    public static NullRate none() {
        return new NullRate(0);
    }

    public static NullRate ofPercent(int percent) {
        return new NullRate(percent);
    }

    /** Fraction in {@code [0.0, 1.0]}, converted to an integer percent. */
    public static NullRate of(double rate) {
        if (rate < 0.0 || rate > 1.0 || Double.isNaN(rate)) {
            throw new IllegalArgumentException("rate must be in [0.0, 1.0], got: " + rate);
        }
        return new NullRate((int) Math.round(rate * 100.0));
    }

    public int percent() {
        return percent;
    }

    public boolean shouldNull(long index) {
        if (percent <= 0) {
            return false;
        }
        if (percent >= 100) {
            return true;
        }
        return (index % 100) < percent;
    }
}
