package io.github.manesioz.streamgen.flink;

import io.github.manesioz.streamgen.Boundedness;
import io.github.manesioz.streamgen.policy.Keying;
import io.github.manesioz.streamgen.policy.Payload;
import io.github.manesioz.streamgen.policy.Time;
import io.github.manesioz.streamgen.row.IndexRowGenerator;
import io.github.manesioz.streamgen.row.Row;
import io.github.manesioz.streamgen.schema.FieldType;
import io.github.manesioz.streamgen.schema.Schema;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlinkDataGenTest {

    @Test
    void buildsADataGeneratorSource() {
        Schema schema = Schema.builder()
                .field("key", FieldType.STRING, Keying.uniform(4))
                .field("ts", FieldType.BIGINT, Time.monotonic(0L, 1L))
                .field("value", FieldType.STRING, Payload.sequence())
                .build();

        DataGeneratorSource<Row> source =
                FlinkDataGen.source(schema).finite(100).build();

        assertNotNull(source);
    }

    @Test
    void adapterMatchesCoreGenerator() throws Exception {
        Schema schema = Schema.builder()
                .field("value", FieldType.STRING, Payload.sequence("v-"))
                .build();
        IndexRowGenerator core = new IndexRowGenerator(schema);
        FlinkRowGeneratorFunction flink = new FlinkRowGeneratorFunction(core);

        assertEquals(core.map(12L), flink.map(12L));
    }

    @Test
    void boundednessIsSharedWithCore() {
        assertEquals(50L, Boundedness.finite(50).count());
    }
}
