package io.streamgen;

import java.io.Serializable;

@FunctionalInterface
public interface Assign<V> extends Serializable {

    V assign(long index);
}
