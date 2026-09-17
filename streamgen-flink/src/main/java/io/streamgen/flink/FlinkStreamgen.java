package io.streamgen.flink;

import io.streamgen.Schema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;

public final class FlinkStreamgen {

    private FlinkStreamgen() {}

    public static <T> DataGeneratorSource<T> source(Schema<T> schema, long count) {
        return source(schema, count, RateLimiterStrategy.noOp());
    }

    public static <T> DataGeneratorSource<T> source(
            Schema<T> schema, long count, RateLimiterStrategy rate) {
        return new DataGeneratorSource<>(
                index -> schema.at(index), count, rate, TypeInformation.of(schema.type()));
    }
}
