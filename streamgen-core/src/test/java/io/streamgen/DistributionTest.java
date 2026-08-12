package io.streamgen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistributionTest {

    enum Status { NEW, PAID, SHIPPED }

    @Test
    void uniformCyclesThroughEverySlot() {
        Distribution d = Distribution.uniform(4);
        assertEquals(List.of(0L, 1L, 2L, 3L, 0L), List.of(d.slot(0), d.slot(1), d.slot(2), d.slot(3), d.slot(4)));
        assertEquals(4, d.size());
    }

    @Test
    void hotSendsPercentOfTrafficToOnePercentOfSlots() {
        Distribution d = Distribution.hot(1_000, 80);
        long hotSlots = Math.max(1, 1_000 / 100);
        int hits = 0;
        for (long i = 0; i < 10_000; i++) {
            long slot = d.slot(i);
            assertTrue(slot >= 0 && slot < 1_000);
            if (slot < hotSlots) {
                hits++;
            }
        }
        assertEquals(8_000, hits);
    }

    @Test
    void sameDistributionSameSkewAcrossColumns() {
        Distribution customers = Distribution.hot(10_000, 80);
        Assign<String> id = customers.prefixed("u-");
        Assign<Long> account = customers.of(Values.index());
        for (long i = 0; i < 100; i++) {
            assertEquals("u-" + account.assign(i), id.assign(i));
        }
    }

    @Test
    void pickRequiresMatchingSize() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Distribution.uniform(2).pick(List.of("a", "b", "c")));
    }

    @Test
    void pickCyclesThroughEveryValue() {
        Assign<Status> status = Distribution.uniform(3).pick(List.of(Status.values()));
        assertEquals(Status.NEW, status.assign(0));
        assertEquals(Status.PAID, status.assign(1));
        assertEquals(Status.SHIPPED, status.assign(2));
        assertEquals(Status.NEW, status.assign(3));
    }

    @Test
    void rejectsInvalidShape() {
        assertThrows(IllegalArgumentException.class, () -> Distribution.uniform(0));
        assertThrows(IllegalArgumentException.class, () -> Distribution.hot(1, 50));
        assertThrows(IllegalArgumentException.class, () -> Distribution.hot(10, 101));
    }
}
