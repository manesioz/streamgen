package io.github.manesioz.streamgen.schema;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.validity.NullRate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Ordered list of named fields. Immutable after {@link Builder#build()}. */
public final class Schema implements Serializable {

    private final List<Field> fields;
    private final String[] names;

    private Schema(List<Field> fields) {
        this.fields = List.copyOf(fields);
        this.names = this.fields.stream().map(Field::name).toArray(String[]::new);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Field> fields() {
        return fields;
    }

    public String[] names() {
        return names.clone();
    }

    public int size() {
        return fields.size();
    }

    public Field field(int index) {
        return fields.get(index);
    }

    public Optional<Field> find(String name) {
        Objects.requireNonNull(name, "name");
        for (Field field : fields) {
            if (field.name().equals(name)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    public Field require(String name) {
        return find(name)
                .orElseThrow(() -> new IllegalArgumentException("unknown field: " + name));
    }

    public static final class Builder {

        private final List<Field> fields = new ArrayList<>();
        private final Set<String> names = new LinkedHashSet<>();

        public Builder field(String name, FieldType type, FieldPolicy policy) {
            return field(new Field(name, type, policy));
        }

        public Builder field(String name, FieldType type, FieldPolicy policy, NullRate nullRate) {
            return field(new Field(name, type, policy, nullRate));
        }

        public Builder field(Field field) {
            if (!names.add(field.name())) {
                throw new IllegalArgumentException("duplicate field name: " + field.name());
            }
            fields.add(field);
            return this;
        }

        public Schema build() {
            if (fields.isEmpty()) {
                throw new IllegalArgumentException("schema must have at least one field");
            }
            return new Schema(Collections.unmodifiableList(new ArrayList<>(fields)));
        }
    }
}
