package io.streamgen;

record Uniform(long size) implements Distribution {

    Uniform {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
    }

    @Override
    public long slot(long index) {
        return Math.floorMod(index, size);
    }
}
