package io.github.manesioz.streamgen.policy;

import java.io.Serializable;

/** Pure rewrite of a field value given the generator index. */
@FunctionalInterface
public interface FieldTransform extends Serializable {

    Object apply(long index, Object innerValue);
}
