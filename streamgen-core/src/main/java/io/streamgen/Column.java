package io.streamgen;

import java.io.Serializable;
import java.util.Objects;

public final class Column implements Serializable {

    private final String name;
    private final Class<?> javaType;
    private final Assign<?> assign;

    public Column(String name, Class<?> javaType, Assign<?> assign) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("column name must be non-blank");
        }
        this.name = name;
        this.javaType = Objects.requireNonNull(javaType, "javaType");
        this.assign = Objects.requireNonNull(assign, "assign");
    }

    public Column(String name, ColType type, Assign<?> assign) {
        this(name, type.javaType(), assign);
    }

    public String name() {
        return name;
    }

    public Class<?> javaType() {
        return javaType;
    }

    public Assign<?> assign() {
        return assign;
    }

    public Column withAssign(Assign<?> next) {
        return new Column(name, javaType, next);
    }
}
