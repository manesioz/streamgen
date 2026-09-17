package io.streamgen;

import java.io.Serializable;

public interface Assign<V> extends Serializable {

    V assign(long index);
}
