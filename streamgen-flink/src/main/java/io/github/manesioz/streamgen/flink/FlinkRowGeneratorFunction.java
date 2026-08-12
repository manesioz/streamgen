package io.github.manesioz.streamgen.flink;

import io.github.manesioz.streamgen.row.IndexRowGenerator;
import io.github.manesioz.streamgen.row.Row;
import org.apache.flink.connector.datagen.source.GeneratorFunction;

/** Adapts the Flink-free {@link IndexRowGenerator} to Flink's {@link GeneratorFunction}. */
public final class FlinkRowGeneratorFunction implements GeneratorFunction<Long, Row> {

    private final IndexRowGenerator generator;

    public FlinkRowGeneratorFunction(IndexRowGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Row map(Long index) {
        return generator.map(index);
    }
}
