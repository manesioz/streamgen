package io.streamgen;

record Hot(long size, int hotPercent, long hotSlots) implements Distribution {

    Hot(long size, int hotPercent) {
        this(size, hotPercent, Math.max(1, size / 100));
    }

    Hot {
        if (size <= 1) {
            throw new IllegalArgumentException("size must be > 1");
        }
        if (hotPercent < 0 || hotPercent > 100) {
            throw new IllegalArgumentException("hotPercent must be in [0, 100]");
        }
    }

    @Override
    public long slot(long index) {
        if (Math.floorMod(index, 100) < hotPercent) {
            return Math.floorMod(index, hotSlots);
        }
        return hotSlots + Math.floorMod(index, size - hotSlots);
    }
}
