package io.github.manesioz.streamgen.policy;

import io.github.manesioz.streamgen.policy.compose.JitterPolicy;
import io.github.manesioz.streamgen.policy.time.MonotonicTimePolicy;

/** Factories for event-time field policies. */
public final class Time {

    private Time() {}

    public static FieldPolicy monotonic(long startMs, long stepMs) {
        return new MonotonicTimePolicy(startMs, stepMs);
    }

    /** Monotonic event time with a deterministic lag of up to {@code maxLagMs}. */
    public static FieldPolicy delayed(long startMs, long stepMs, long maxLagMs) {
        return new JitterPolicy(new MonotonicTimePolicy(startMs, stepMs), maxLagMs);
    }
}
