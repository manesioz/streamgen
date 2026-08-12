package io.streamgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClockTest {

    @Test
    void steppingAdvancesLinearly() {
        Assign<Long> clock = Clock.stepping(1_000L, 100L);
        assertEquals(1_000L, clock.assign(0));
        assertEquals(1_500L, clock.assign(5));
    }

    @Test
    void outOfOrderNeverAdvancesPastBaseAndStaysBounded() {
        Assign<Long> base = Clock.stepping(10_000L, 100L);
        Assign<Long> clock = Clock.outOfOrder(base, 250L);
        for (long i = 0; i < 500; i++) {
            long lag = base.assign(i) - clock.assign(i);
            assertTrue(lag >= 0 && lag <= 250L, "lag out of bounds at " + i + ": " + lag);
        }
    }

    @Test
    void lateFiresOnlyOnSchedule() {
        Assign<Long> base = Clock.stepping(0L, 1_000L);
        Assign<Long> clock = Clock.late(base, 10, 60_000L);
        assertEquals(base.assign(9), clock.assign(9));
        assertEquals(base.assign(10) - 60_000L, clock.assign(10));
        assertEquals(base.assign(0), clock.assign(0));
    }

    @Test
    void deterministic() {
        Assign<Long> a = Clock.late(Clock.outOfOrder(Clock.stepping(0L, 1_000L), 30_000L), 100, 120_000L);
        Assign<Long> b = Clock.late(Clock.outOfOrder(Clock.stepping(0L, 1_000L), 30_000L), 100, 120_000L);
        for (long i = 0; i < 1_000; i++) {
            assertEquals(a.assign(i), b.assign(i));
        }
    }
}
