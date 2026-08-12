package io.github.manesioz.streamgen.policy;

import io.github.manesioz.streamgen.policy.payload.CatalogPayloadPolicy;
import io.github.manesioz.streamgen.policy.payload.SeededDoubleRangePolicy;
import io.github.manesioz.streamgen.policy.payload.SeededLongRangePolicy;
import io.github.manesioz.streamgen.policy.payload.SequencePayloadPolicy;

/** Factories for payload field policies. */
public final class Payload {

    private Payload() {}

    public static FieldPolicy sequence() {
        return new SequencePayloadPolicy();
    }

    public static FieldPolicy sequence(String prefix) {
        return new SequencePayloadPolicy(prefix);
    }

    public static FieldPolicy catalog(String... values) {
        return new CatalogPayloadPolicy(values);
    }

    public static FieldPolicy seededLongRange(long seed, long min, long max) {
        return new SeededLongRangePolicy(seed, min, max);
    }

    public static FieldPolicy seededDoubleRange(long seed, double min, double max) {
        return new SeededDoubleRangePolicy(seed, min, max);
    }
}
