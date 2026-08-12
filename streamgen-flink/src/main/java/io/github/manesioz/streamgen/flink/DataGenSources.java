package io.github.manesioz.streamgen.flink;

import io.github.manesioz.streamgen.Boundedness;
import io.github.manesioz.streamgen.model.KeyedTimedRecord;
import io.github.manesioz.streamgen.preset.KeyedTimedRecords;
import io.github.manesioz.streamgen.preset.Schemas;
import io.github.manesioz.streamgen.row.IndexRowGenerator;
import io.github.manesioz.streamgen.row.Row;
import io.github.manesioz.streamgen.schema.Schema;
import io.github.manesioz.streamgen.validity.NullRate;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;

/** Convenience factories for common sources. Prefer {@link FlinkDataGen} for new usage. */
public final class DataGenSources {

    private DataGenSources() {}

    public static DataGeneratorSource<Row> rows(
            Schema schema, Boundedness boundedness, RateLimiterStrategy rateLimiterStrategy) {
        return FlinkDataGen.source(schema).bounded(boundedness).rate(rateLimiterStrategy).build();
    }

    public static DataGeneratorSource<KeyedTimedRecord> keyedTimed(
            int numKeys,
            long startMs,
            long stepMs,
            NullRate nullRate,
            Boundedness boundedness,
            RateLimiterStrategy rateLimiterStrategy) {
        Schema schema = Schemas.keyedTimed(numKeys, startMs, stepMs, nullRate);
        IndexRowGenerator rows = new IndexRowGenerator(schema);
        GeneratorFunction<Long, KeyedTimedRecord> generator =
                index -> KeyedTimedRecords.fromRow(rows.map(index));
        return new DataGeneratorSource<>(
                generator,
                boundedness.count(),
                rateLimiterStrategy,
                Types.POJO(KeyedTimedRecord.class));
    }
}
