package io.streamgen;

public enum ColType {
    STRING(String.class),
    INT(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    BOOLEAN(Boolean.class);

    private final Class<?> javaType;

    ColType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> javaType() {
        return javaType;
    }
}
