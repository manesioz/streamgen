package io.github.manesioz.streamgen.policy.keying;

import io.github.manesioz.streamgen.policy.FieldPolicy;
import io.github.manesioz.streamgen.schema.FieldType;

/**
 * Skewed keys: a configurable share of indexes map to one hot key; remainder
 * spread across the other keys. Deterministic in {@code index}.
 */
public final class HotKeyPolicy implements FieldPolicy {

    private final int numKeys;
    private final int hotKeyIndex;
    private final int hotPercent;

    public HotKeyPolicy(int numKeys, int hotKeyIndex, int hotPercent) {
        if (numKeys < 2) {
            throw new IllegalArgumentException("numKeys must be >= 2 for skew, got: " + numKeys);
        }
        if (hotKeyIndex < 0 || hotKeyIndex >= numKeys) {
            throw new IllegalArgumentException(
                    "hotKeyIndex must be in [0, numKeys), got: " + hotKeyIndex);
        }
        if (hotPercent < 1 || hotPercent > 99) {
            throw new IllegalArgumentException(
                    "hotPercent must be in [1, 99], got: " + hotPercent);
        }
        this.numKeys = numKeys;
        this.hotKeyIndex = hotKeyIndex;
        this.hotPercent = hotPercent;
    }

    public HotKeyPolicy(int numKeys, int hotPercent) {
        this(numKeys, 0, hotPercent);
    }

    @Override
    public Object assign(long index) {
        if ((index % 100) < hotPercent) {
            return "key-" + hotKeyIndex;
        }
        int coldCount = numKeys - 1;
        int coldOffset = (int) (index % coldCount);
        int keyIndex = coldOffset < hotKeyIndex ? coldOffset : coldOffset + 1;
        return "key-" + keyIndex;
    }

    @Override
    public FieldType producedType() {
        return FieldType.STRING;
    }
}
