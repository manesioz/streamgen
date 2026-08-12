package io.github.manesioz.streamgen.policy.payload;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.schema.FieldType;

import java.util.List;

/** Cycles through a fixed catalog: {@code values[index % size]}. */
public final class CatalogPayloadPolicy implements FieldPolicy {

    private final List<String> values;

    public CatalogPayloadPolicy(List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("catalog must be non-empty");
        }
        for (String value : values) {
            if (value == null) {
                throw new IllegalArgumentException("catalog values must be non-null");
            }
        }
        this.values = List.copyOf(values);
    }

    public CatalogPayloadPolicy(String... values) {
        this(List.of(values));
    }

    @Override
    public Object assign(long index) {
        return values.get((int) (index % values.size()));
    }

    @Override
    public FieldType producedType() {
        return FieldType.STRING;
    }
}
