package io.github.manesioz.streamgen.policy.compose;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.policy.Keying;
import io.github.manesioz.streamgen.policy.Payload;
import io.github.manesioz.streamgen.policy.Policies;
import io.github.manesioz.streamgen.policy.Time;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliciesTest {

    @Test
    void mixIsDeterministicAndUsesBothSides() {
        FieldPolicy mixed = Policies.mix(Payload.sequence("a-"), 1, Payload.sequence("b-"), 1);
        FieldPolicy again = Policies.mix(Payload.sequence("a-"), 1, Payload.sequence("b-"), 1);

        int a = 0;
        int b = 0;
        for (long i = 0; i < 200; i++) {
            assertEquals(mixed.assign(i), again.assign(i));
            String value = (String) mixed.assign(i);
            if (value.startsWith("a-")) {
                a++;
            } else if (value.startsWith("b-")) {
                b++;
            }
        }
        assertTrue(a > 0);
        assertTrue(b > 0);
        assertEquals(200, a + b);
    }

    @Test
    void duplicateReplaysAPriorValue() {
        FieldPolicy inner = Payload.sequence("v-");
        FieldPolicy dup = Policies.duplicate(inner, 50, 3);

        int duplicates = 0;
        for (long i = 1; i < 200; i++) {
            String value = (String) dup.assign(i);
            if (!value.equals("v-" + i)) {
                duplicates++;
                long source = Long.parseLong(value.substring(2));
                assertTrue(source >= 0 && source < i);
                assertTrue(i - source <= 3);
            }
        }
        assertTrue(duplicates > 0);
        assertEquals(dup.assign(17L), Policies.duplicate(inner, 50, 3).assign(17L));
    }

    @Test
    void jitterNeverAdvancesEventTime() {
        FieldPolicy delayed = Policies.jitter(Time.monotonic(10_000L, 100L), 250L);
        for (long i = 0; i < 50; i++) {
            long expected = 10_000L + i * 100L;
            long actual = (Long) delayed.assign(i);
            assertTrue(actual <= expected);
            assertTrue(expected - actual <= 250L);
        }
        assertEquals(delayed.assign(9L), Policies.jitter(Time.monotonic(10_000L, 100L), 250L).assign(9L));
    }

    @Test
    void andThenCanRewriteValues() {
        FieldPolicy tagged = Payload.sequence("v-").andThen((index, value) -> value + "-x");
        assertEquals("v-4-x", tagged.assign(4));
        assertEquals(tagged.assign(4), Payload.sequence("v-").andThen((index, value) -> value + "-x").assign(4));
    }

    @Test
    void mixRejectsTypeMismatch() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Policies.mix(Keying.uniform(2), 1, Time.monotonic(0L, 1L), 1));
    }

    @Test
    void jitterRejectsNonBigint() {
        assertThrows(IllegalArgumentException.class, () -> Policies.jitter(Payload.sequence(), 10));
    }

    @Test
    void duplicateDoesNotEqualInnerOnHitIndexes() {
        FieldPolicy inner = Payload.sequence("v-");
        FieldPolicy dup = Policies.duplicate(inner, 90, 1);
        boolean sawDifference = false;
        for (long i = 1; i < 50; i++) {
            if (!dup.assign(i).equals(inner.assign(i))) {
                sawDifference = true;
                break;
            }
        }
        assertTrue(sawDifference);
        assertEquals("v-0", dup.assign(0));
    }
}
