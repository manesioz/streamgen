package io.github.manesioz.streamgen.preset;

import io.github.manesioz.streamgen.policy.Keying;
import io.github.manesioz.streamgen.policy.Payload;
import io.github.manesioz.streamgen.policy.Time;
import io.github.manesioz.streamgen.schema.FieldType;
import io.github.manesioz.streamgen.schema.Schema;
import io.github.manesioz.streamgen.validity.NullRate;

/** Ready-made schemas for keyed, event-timed streams. */
public final class Schemas {

    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String TIMESTAMP = "timestamp";

    private Schemas() {}

    public static Schema keyedTimed(int numKeys, long startMs, long stepMs) {
        return keyedTimed(numKeys, startMs, stepMs, NullRate.none());
    }

    public static Schema keyedTimed(int numKeys, long startMs, long stepMs, NullRate valueNullRate) {
        return Schema.builder()
                .field(KEY, FieldType.STRING, Keying.uniform(numKeys))
                .field(VALUE, FieldType.STRING, Payload.sequence(), valueNullRate)
                .field(TIMESTAMP, FieldType.BIGINT, Time.monotonic(startMs, stepMs))
                .build();
    }
}
