package io.github.manesioz.streamgen.schema;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.validity.NullRate;

import java.io.Serializable;
import java.util.Objects;

/** One named column in a {@link Schema}. */
public final class Field implements Serializable {

    private final String name;
    private final FieldType type;
    private final FieldPolicy policy;
    private final NullRate nullRate;

    public Field(String name, FieldType type, FieldPolicy policy, NullRate nullRate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("field name must be non-blank");
        }
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(policy, "policy");
        if (policy.producedType() != type) {
            throw new IllegalArgumentException(
                    "field '"
                            + name
                            + "' type "
                            + type
                            + " incompatible with policy producing "
                            + policy.producedType());
        }
        this.name = name;
        this.type = type;
        this.policy = policy;
        this.nullRate = nullRate == null ? NullRate.none() : nullRate;
    }

    public Field(String name, FieldType type, FieldPolicy policy) {
        this(name, type, policy, NullRate.none());
    }

    public String name() {
        return name;
    }

    public FieldType type() {
        return type;
    }

    public FieldPolicy policy() {
        return policy;
    }

    public NullRate nullRate() {
        return nullRate;
    }
}
