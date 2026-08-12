package io.github.manesioz.streamgen.row;

import io.github.manesioz.streamgen.policy.Keying;
import io.github.manesioz.streamgen.policy.Payload;
import io.github.manesioz.streamgen.policy.Time;
import io.github.manesioz.streamgen.schema.FieldType;
import io.github.manesioz.streamgen.schema.Schema;
import io.github.manesioz.streamgen.validity.NullRate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexRowGeneratorTest {

    @Test
    void mapsIndexThroughFieldPolicies() {
        Schema schema = Schema.builder()
                .field("user_id", FieldType.STRING, Keying.uniform(3))
                .field("ts", FieldType.BIGINT, Time.monotonic(1_000L, 500L))
                .field("page", FieldType.STRING, Payload.catalog("home", "cart"))
                .build();

        IndexRowGenerator generator = new IndexRowGenerator(schema);

        Row r0 = generator.map(0L);
        assertEquals("key-0", r0.getString("user_id"));
        assertEquals(1_000L, r0.getLong("ts"));
        assertEquals("home", r0.getString("page"));

        Row r3 = generator.map(3L);
        assertEquals("key-0", r3.getString("user_id"));
        assertEquals(1_000L + 3 * 500L, r3.getLong("ts"));
        assertEquals("cart", r3.getString("page"));
    }

    @Test
    void appliesNullRatePerField() {
        Schema schema = Schema.builder()
                .field("user_id", FieldType.STRING, Keying.uniform(2))
                .field("page", FieldType.STRING, Payload.sequence("v-"), NullRate.ofPercent(10))
                .build();

        IndexRowGenerator generator = new IndexRowGenerator(schema);

        assertNull(generator.map(0L).getString("page"));
        assertNull(generator.map(9L).getString("page"));
        assertEquals("v-10", generator.map(10L).getString("page"));
        assertEquals("key-0", generator.map(0L).getString("user_id"));
    }

    @Test
    void isDeterministicForSameIndex() {
        Schema schema = Schema.builder()
                .field("id", FieldType.BIGINT, Payload.seededLongRange(42L, 1L, 100L))
                .field("score", FieldType.DOUBLE, Payload.seededDoubleRange(7L, 0.0, 1.0))
                .build();

        IndexRowGenerator a = new IndexRowGenerator(schema);
        IndexRowGenerator b = new IndexRowGenerator(schema);

        assertEquals(a.map(17L), b.map(17L));
        assertEquals(a.map(99L), b.map(99L));
    }

    @Test
    void materializesAFiniteList() {
        Schema schema = Schema.builder()
                .field("id", FieldType.STRING, Payload.sequence("v-"))
                .build();
        List<Row> rows = new IndexRowGenerator(schema).list(3);
        assertEquals(3, rows.size());
        assertEquals("v-0", rows.get(0).getString("id"));
        assertEquals("v-2", rows.get(2).getString("id"));
    }

    @Test
    void rejectsTypeMismatch() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Schema.builder()
                        .field("ts", FieldType.STRING, Time.monotonic(0L, 1L))
                        .build());
    }

    @Test
    void rejectsDuplicateFieldNames() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Schema.builder()
                        .field("a", FieldType.STRING, Payload.sequence())
                        .field("a", FieldType.STRING, Payload.sequence())
                        .build());
    }
}
