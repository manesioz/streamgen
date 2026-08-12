package io.github.manesioz.streamgen.policy;

import io.github.manesioz.streamgen.schema.FieldType;

import java.io.Serializable;

/**
 * Maps a generator index to a field value.
 *
 * <p><b>Invariant:</b> {@link #assign(long)} is a pure function of {@code (index, this
 * policy's config)}. Same index + same policy must always yield the same value, including
 * after restore, replay, and parallel split of the index space. Do not capture wall-clock
 * time, thread locals, or mutable counters.
 */
@FunctionalInterface
public interface FieldPolicy extends Serializable {

    Object assign(long index);

    default FieldType producedType() {
        return FieldType.STRING;
    }

    /**
     * Decorate this policy. The transform sees both the index and the inner value so it
     * can jitter, duplicate, or rewrite without reimplementing the base generator.
     */
    default FieldPolicy andThen(FieldTransform transform) {
        return new AndThenPolicy(this, transform);
    }

    /** Named wrapper so Flink Kryo/Java serialization does not depend on lambdas capturing {@code this}. */
    final class AndThenPolicy implements FieldPolicy {

        private final FieldPolicy inner;
        private final FieldTransform transform;

        AndThenPolicy(FieldPolicy inner, FieldTransform transform) {
            this.inner = inner;
            this.transform = transform;
        }

        @Override
        public Object assign(long index) {
            return transform.apply(index, inner.assign(index));
        }

        @Override
        public FieldType producedType() {
            return inner.producedType();
        }
    }
}
