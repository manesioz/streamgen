package io.streamgen.flink;

import io.streamgen.ColType;
import io.streamgen.Row;
import io.streamgen.Schema;
import io.streamgen.assign.Id;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlinkStreamgenTest {

    public record Order(String customerId, long ts) {}

    @Test
    void buildsADataGeneratorSource() {
        Schema<Order> schema = Schema.of(Order.class).assign("customerId", Id.even(8, "u-"));
        DataGeneratorSource<Order> source = FlinkStreamgen.source(schema, 100);
        assertNotNull(source);
    }

    @Test
    void adapterMatchesSchemaAt() {
        Schema<Row> schema =
                Schema.builder().column("id", ColType.STRING, Id.even(3, "k-")).build();
        assertEquals(schema.at(4).get("id"), schema.at(4).get("id"));
        assertEquals("k-1", schema.at(4).get("id"));
    }
}
