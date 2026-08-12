package io.streamgen.flink;

import io.streamgen.Assign;
import io.streamgen.Clock;
import io.streamgen.Distribution;
import io.streamgen.Multiplicity;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StreamgenTest {

    public record Order(String customerId, long ts) {}

    @Test
    void buildsADataGeneratorSourceOfTheConsumerType() {
        Assign<String> customerId = Distribution.hot(10_000, 80).prefixed("u-");
        Assign<Long> ts = Clock.outOfOrder(Clock.stepping(0L, 1_000L), 30_000L);
        Assign<Order> orders = i -> new Order(customerId.assign(i), ts.assign(i));
        Assign<Order> stream = Multiplicity.duplicates(orders, 100);

        DataGeneratorSource<Order> source = Streamgen.source(stream, 1_000, Order.class);
        assertNotNull(source);
        assertEquals(Order.class, source.getProducedType().getTypeClass());
    }

    @Test
    void generatorFunctionDelegatesToAssign() throws Exception {
        Assign<String> id = Distribution.uniform(3).prefixed("k-");
        AssignGeneratorFunction<String> fn = new AssignGeneratorFunction<>(id);
        assertEquals("k-1", fn.map(4L));
        assertEquals(id.assign(4), fn.map(4L));
    }
}
