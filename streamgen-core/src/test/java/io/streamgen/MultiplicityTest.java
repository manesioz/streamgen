package io.streamgen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MultiplicityTest {

    record Order(String customerId, long ts, String status) {}

    @Test
    void duplicatesRepeatThePreviousRowExactly() {
        Distribution customers = Distribution.hot(1_000, 80);
        Assign<String> customerId = customers.prefixed("u-");
        Assign<Long> ts = Clock.outOfOrder(Clock.stepping(0L, 1_000L), 5_000L);
        Assign<String> status = Distribution.uniform(2).pick(List.of("new", "paid"));

        Assign<Order> orders = i -> new Order(customerId.assign(i), ts.assign(i), status.assign(i));
        Assign<Order> stream = Multiplicity.duplicates(orders, 100);

        assertEquals(stream.assign(99), stream.assign(100));
        assertNotEquals(stream.assign(100), stream.assign(101));
        assertEquals(orders.assign(57), stream.assign(57));
    }
}
