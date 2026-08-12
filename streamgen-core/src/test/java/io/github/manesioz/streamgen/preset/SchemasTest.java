package io.github.manesioz.streamgen.preset;

import io.github.manesioz.streamgen.model.KeyedTimedRecord;
import io.github.manesioz.streamgen.row.IndexRowGenerator;
import io.github.manesioz.streamgen.row.Row;
import io.github.manesioz.streamgen.schema.Schema;
import io.github.manesioz.streamgen.validity.NullRate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SchemasTest {

    @Test
    void keyedTimedPresetMatchesLegacyShape() {
        Schema schema = Schemas.keyedTimed(3, 1_000L, 500L);
        IndexRowGenerator generator = new IndexRowGenerator(schema);

        Row row = generator.map(3L);
        KeyedTimedRecord record = KeyedTimedRecords.fromRow(row);

        assertEquals("key-0", record.getKey());
        assertEquals("v-3", record.getValue());
        assertEquals(1_000L + 3 * 500L, record.getTimestamp());
    }

    @Test
    void keyedTimedPresetAppliesNullRateToValue() {
        Schema schema = Schemas.keyedTimed(2, 0L, 1L, NullRate.ofPercent(10));
        IndexRowGenerator generator = new IndexRowGenerator(schema);

        assertNull(KeyedTimedRecords.fromRow(generator.map(0L)).getValue());
        assertEquals("v-10", KeyedTimedRecords.fromRow(generator.map(10L)).getValue());
    }
}
