package io.github.manesioz.streamgen.policy.payload;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.schema.FieldType;

/** Deterministic sequence payload: {@code prefix + index}. */
public final class SequencePayloadPolicy implements FieldPolicy {

    private final String prefix;

    public SequencePayloadPolicy() {
        this("v-");
    }

    public SequencePayloadPolicy(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }
        this.prefix = prefix;
    }

    @Override
    public Object assign(long index) {
        return prefix + index;
    }

    @Override
    public FieldType producedType() {
        return FieldType.STRING;
    }
}
