package io.github.manesioz.streamgen.policy.keying;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotKeyPolicyTest {

    @Test
    void routesConfiguredShareToHotKey() {
        HotKeyPolicy policy = new HotKeyPolicy(4, 0, 80);

        assertEquals("key-0", policy.assign(0));
        assertEquals("key-0", policy.assign(79));
        assertTrue(policy.assign(80).toString().startsWith("key-"));
        assertTrue(!policy.assign(80).equals("key-0"));
    }

    @Test
    void approximatesHotPercentOverAWindow() {
        HotKeyPolicy policy = new HotKeyPolicy(5, 0, 80);
        int hot = 0;
        int total = 1000;
        for (long i = 0; i < total; i++) {
            if ("key-0".equals(policy.assign(i))) {
                hot++;
            }
        }
        assertEquals(800, hot);
    }

    @Test
    void spreadsColdTrafficAcrossNonHotKeys() {
        HotKeyPolicy policy = new HotKeyPolicy(4, 0, 80);
        Map<String, Integer> coldCounts = new HashMap<>();
        for (long i = 0; i < 1000; i++) {
            String key = (String) policy.assign(i);
            if (!"key-0".equals(key)) {
                coldCounts.merge(key, 1, Integer::sum);
            }
        }
        assertEquals(3, coldCounts.size());
        assertTrue(coldCounts.containsKey("key-1"));
        assertTrue(coldCounts.containsKey("key-2"));
        assertTrue(coldCounts.containsKey("key-3"));
    }

    @Test
    void supportsNonZeroHotKeyIndex() {
        HotKeyPolicy policy = new HotKeyPolicy(3, 2, 50);
        assertEquals("key-2", policy.assign(0));
        assertEquals("key-2", policy.assign(49));
        String cold = (String) policy.assign(50);
        assertTrue(cold.equals("key-0") || cold.equals("key-1"));
    }

    @Test
    void rejectsInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> new HotKeyPolicy(1, 80));
        assertThrows(IllegalArgumentException.class, () -> new HotKeyPolicy(3, 3, 80));
        assertThrows(IllegalArgumentException.class, () -> new HotKeyPolicy(3, 0, 100));
    }
}
