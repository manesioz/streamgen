package io.streamgen.flink;

import io.streamgen.Assign;
import org.apache.flink.connector.datagen.source.GeneratorFunction;

import java.util.Objects;

public final class AssignGeneratorFunction<T> implements GeneratorFunction<Long, T> {

    private final Assign<T> assign;

    public AssignGeneratorFunction(Assign<T> assign) {
        this.assign = Objects.requireNonNull(assign, "assign");
    }

    @Override
    public T map(Long index) {
        return assign.assign(index);
    }
}
