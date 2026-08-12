package io.github.manesioz.streamgen.flink;

import io.github.manesioz.streamgen.Boundedness;
import io.github.manesioz.streamgen.row.IndexRowGenerator;
import io.github.manesioz.streamgen.row.Row;
import io.github.manesioz.streamgen.schema.Schema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;

import java.util.Objects;

public final class FlinkDataGen {

    private final Schema schema;
    private Boundedness boundedness = Boundedness.unbounded();
    private RateLimiterStrategy rateLimiterStrategy = RateLimiterStrategy.noOp();

    private FlinkDataGen(Schema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    public static FlinkDataGen source(Schema schema) {
        return new FlinkDataGen(schema);
    }

    public FlinkDataGen bounded(Boundedness boundedness) {
        this.boundedness = Objects.requireNonNull(boundedness, "boundedness");
        return this;
    }

    public FlinkDataGen finite(long count) {
        return bounded(Boundedness.finite(count));
    }

    public FlinkDataGen rate(RateLimiterStrategy rateLimiterStrategy) {
        this.rateLimiterStrategy =
                Objects.requireNonNull(rateLimiterStrategy, "rateLimiterStrategy");
        return this;
    }

    public DataGeneratorSource<Row> build() {
        return new DataGeneratorSource<>(
                new FlinkRowGeneratorFunction(new IndexRowGenerator(schema)),
                boundedness.count(),
                rateLimiterStrategy,
                TypeInformation.of(Row.class));
    }
}
