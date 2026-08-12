package io.streamgen.flink;

import io.streamgen.Assign;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;

public final class Streamgen {

    private Streamgen() {}

    public static <T> DataGeneratorSource<T> source(Assign<T> assign, long count, Class<T> type) {
        return source(assign, count, RateLimiterStrategy.noOp(), type);
    }

    public static <T> DataGeneratorSource<T> source(
            Assign<T> assign, long count, RateLimiterStrategy rate, Class<T> type) {
        return new DataGeneratorSource<>(
                new AssignGeneratorFunction<>(assign), count, rate, TypeInformation.of(type));
    }
}
