package io.github.manesioz.streamgen.policy.keying;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.schema.FieldType;

/** Round-robin keys: {@code "key-" + (index % numKeys)}. */
public final class UniformKeyPolicy implements FieldPolicy {

    private final int numKeys;

    public UniformKeyPolicy(int numKeys) {
        if (numKeys <= 0) {
            throw new IllegalArgumentException("numKeys must be > 0, got: " + numKeys);
        }
        this.numKeys = numKeys;
    }

    public int numKeys() {
        return numKeys;
    }

    @Override
    public Object assign(long index) {
        return "key-" + (index % numKeys);
    }

    @Override
    public FieldType producedType() {
        return FieldType.STRING;
    }
}
