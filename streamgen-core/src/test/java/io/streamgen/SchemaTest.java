package io.streamgen;

import io.streamgen.assign.EventTime;
import io.streamgen.assign.Id;
import io.streamgen.assign.Numbers;
import io.streamgen.assign.Strings;
import io.streamgen.assign.Wrap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTest {

    public record Order(String customerId, long ts, double amount, boolean flag) {}

    public static class BeanOrder {
        private String customerId;
        private long ts;

        public BeanOrder() {}

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }
    }

    @Test
    void pojoGetsBoringFills() {
        Schema<Order> schema = Schema.of(Order.class);
        Order order = schema.at(3);
        assertEquals("customerId-3", order.customerId());
        assertEquals(3L, order.ts());
        assertEquals(3.0, order.amount());
        assertEquals(false, order.flag());
    }

    @Test
    void pojoOverrideReplacesOneColumn() {
        Schema<Order> schema = Schema.of(Order.class).assign("customerId", Id.even(10, "user-"));
        assertEquals("user-3", schema.at(3).customerId());
        assertEquals(3L, schema.at(3).ts());
    }

    @Test
    void pojoPathIsDeterministic() {
        Schema<Order> a = Schema.of(Order.class).assign("ts", EventTime.millis(1000, 10));
        Schema<Order> b = Schema.of(Order.class).assign("ts", EventTime.millis(1000, 10));
        assertEquals(a.at(17), b.at(17));
    }

    @Test
    void beanPathUsesSetters() {
        Schema<BeanOrder> schema =
                Schema.of(BeanOrder.class).assign("customerId", Id.even(4, "c-"));
        BeanOrder order = schema.at(5);
        assertEquals("c-1", order.getCustomerId());
        assertEquals(5L, order.getTs());
    }

    @Test
    void handBuiltRowSchema() {
        Schema<Row> schema = Schema.builder()
                .column("customerId", ColType.STRING, Id.hot(4, 80, "k-"))
                .column("ts", ColType.LONG, EventTime.millis(1_000L, 500L))
                .column("status", ColType.STRING, Strings.oneOf("new", "paid"))
                .column("amount", ColType.DOUBLE, Numbers.between(1.0, 500.0))
                .build();
        Row row = schema.at(0);
        assertEquals("k-0", row.get("customerId", String.class));
        assertEquals(1_000L, row.get("ts", Long.class));
        assertEquals("new", row.get("status", String.class));
    }

    @Test
    void listMaterializesIndexes() {
        List<Order> rows = Schema.of(Order.class).list(3);
        assertEquals(3, rows.size());
        assertEquals("customerId-2", rows.get(2).customerId());
    }

    @Test
    void unknownColumnFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Schema.of(Order.class).assign("nope", Id.even(2)));
    }

    @Test
    void eventTimeLagNeverAdvances() {
        EventTime delayed = EventTime.millis(10_000L, 100L).lag(250L);
        for (long i = 0; i < 50; i++) {
            long expected = 10_000L + i * 100L;
            long actual = delayed.assign(i);
            assertTrue(actual <= expected);
            assertTrue(expected - actual <= 250L);
        }
        assertEquals(delayed.assign(9L), EventTime.millis(10_000L, 100L).lag(250L).assign(9L));
    }

    @Test
    void hotKeysMatchPercent() {
        Assign<String> hot = Id.hot(5, 80);
        int hits = 0;
        for (long i = 0; i < 1000; i++) {
            if ("0".equals(hot.assign(i))) {
                hits++;
            }
        }
        assertEquals(800, hits);
    }

    @Test
    void wrapNullsIsDeterministic() {
        Assign<String> inner = Strings.prefix("n-");
        Assign<String> nullable = Wrap.nulls(0.5, inner);
        int nulls = 0;
        for (long i = 0; i < 200; i++) {
            if (nullable.assign(i) == null) {
                nulls++;
            }
        }
        assertTrue(nulls > 0);
        assertEquals(nullable.assign(7L), Wrap.nulls(0.5, inner).assign(7L));
    }

    @Test
    void wrapAgainReplaysEarlierIndex() {
        Assign<String> again = Wrap.again(1.0, Strings.prefix("v-"));
        assertEquals("v-0", again.assign(0));
        assertEquals("v-4", again.assign(5));
        assertNull(Wrap.nulls(1.0, Strings.prefix("v-")).assign(3));
    }
}
