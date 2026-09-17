package io.streamgen;

import io.streamgen.assign.Numbers;
import io.streamgen.assign.Strings;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Schema<T> implements Serializable {

    private final Class<T> type;
    private final List<Column> columns;

    private Schema(Class<T> type, List<Column> columns) {
        this.type = Objects.requireNonNull(type, "type");
        this.columns = List.copyOf(columns);
    }

    public static <T> Schema<T> of(Class<T> type) {
        Objects.requireNonNull(type, "type");
        List<Column> columns = introspect(type);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("no columns on " + type.getName());
        }
        return new Schema<>(type, columns);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Class<T> type() {
        return type;
    }

    public List<Column> columns() {
        return columns;
    }

    public Schema<T> assign(String column, Assign<?> fill) {
        Objects.requireNonNull(column, "column");
        Objects.requireNonNull(fill, "fill");
        List<Column> next = new ArrayList<>(columns.size());
        boolean found = false;
        for (Column col : columns) {
            if (col.name().equals(column)) {
                checkCompatible(col, fill);
                next.add(col.withAssign(fill));
                found = true;
            } else {
                next.add(col);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("unknown column: " + column);
        }
        return new Schema<>(type, next);
    }

    public T at(long index) {
        Object[] values = new Object[columns.size()];
        String[] names = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            names[i] = col.name();
            Object value = col.assign().assign(index);
            if (value != null && !compatible(col.javaType(), value)) {
                throw new IllegalArgumentException(
                        "column '"
                                + col.name()
                                + "' expected "
                                + col.javaType().getName()
                                + " but assign produced "
                                + value.getClass().getName());
            }
            values[i] = value;
        }
        if (type == Row.class) {
            return type.cast(new Row(names, values));
        }
        if (type.isRecord()) {
            return instantiateRecord(values);
        }
        return instantiateBean(values);
    }

    public List<T> list(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("list() cannot materialize more than Integer.MAX_VALUE rows");
        }
        List<T> rows = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            rows.add(at(i));
        }
        return rows;
    }

    private T instantiateRecord(Object[] values) {
        try {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] types = new Class<?>[components.length];
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                args[i] = coerce(types[i], values[i]);
            }
            Constructor<T> ctor = type.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to instantiate record " + type.getName(), e);
        }
    }

    private T instantiateBean(Object[] values) {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            T instance = ctor.newInstance();
            for (int i = 0; i < columns.size(); i++) {
                setProperty(instance, columns.get(i).name(), columns.get(i).javaType(), values[i]);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to instantiate " + type.getName(), e);
        }
    }

    private static void setProperty(Object target, String name, Class<?> javaType, Object value)
            throws ReflectiveOperationException {
        Object coerced = coerce(javaType, value);
        String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method method = target.getClass().getMethod(setter, javaType);
            method.invoke(target, coerced);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, coerced);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object coerce(Class<?> expected, Object value) {
        if (value == null) {
            return null;
        }
        if (expected.isInstance(value) || box(expected).isInstance(value)) {
            return value;
        }
        if ((expected == int.class || expected == Integer.class) && value instanceof Number n) {
            return n.intValue();
        }
        if ((expected == long.class || expected == Long.class) && value instanceof Number n) {
            return n.longValue();
        }
        if ((expected == double.class || expected == Double.class) && value instanceof Number n) {
            return n.doubleValue();
        }
        if ((expected == boolean.class || expected == Boolean.class) && value instanceof Boolean) {
            return value;
        }
        throw new IllegalArgumentException(
                "cannot coerce " + value.getClass().getName() + " to " + expected.getName());
    }

    private static void checkCompatible(Column column, Assign<?> fill) {
        Object sample = fill.assign(0);
        if (sample != null && !compatible(column.javaType(), sample)) {
            throw new IllegalArgumentException(
                    "column '"
                            + column.name()
                            + "' is "
                            + column.javaType().getName()
                            + " but assign produced "
                            + sample.getClass().getName());
        }
    }

    private static boolean compatible(Class<?> expected, Object value) {
        Class<?> boxed = box(expected);
        if (boxed.isInstance(value)) {
            return true;
        }
        if (value instanceof Number
                && (boxed == Integer.class || boxed == Long.class || boxed == Double.class)) {
            return true;
        }
        return expected.isEnum() && expected.isInstance(value);
    }

    private static Class<?> box(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        return type;
    }

    private static List<Column> introspect(Class<?> type) {
        if (type.isRecord()) {
            List<Column> columns = new ArrayList<>();
            for (RecordComponent component : type.getRecordComponents()) {
                columns.add(columnFor(component.getName(), component.getType()));
            }
            return columns;
        }
        List<Column> columns = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            columns.add(columnFor(field.getName(), field.getType()));
        }
        return columns;
    }

    private static Column columnFor(String name, Class<?> javaType) {
        return new Column(name, javaType, boring(name, javaType));
    }

    static Assign<?> boring(String name, Class<?> javaType) {
        if (javaType == String.class) {
            return Strings.prefix(name + "-");
        }
        if (javaType == int.class || javaType == Integer.class) {
            return Numbers.indexInt();
        }
        if (javaType == long.class || javaType == Long.class) {
            return Numbers.index();
        }
        if (javaType == double.class || javaType == Double.class) {
            return index -> (double) index;
        }
        if (javaType == boolean.class || javaType == Boolean.class) {
            return index -> index % 2 == 0;
        }
        if (javaType.isEnum()) {
            Object[] constants = javaType.getEnumConstants();
            if (constants == null || constants.length == 0) {
                throw new IllegalArgumentException("enum " + javaType.getName() + " has no constants");
            }
            return index -> constants[(int) (index % constants.length)];
        }
        throw new IllegalArgumentException(
                "no boring fill for column '" + name + "' of type " + javaType.getName());
    }

    public static final class Builder {

        private final List<Column> columns = new ArrayList<>();
        private final Set<String> names = new LinkedHashSet<>();

        public Builder column(String name, ColType type, Assign<?> assign) {
            if (!names.add(name)) {
                throw new IllegalArgumentException("duplicate column: " + name);
            }
            columns.add(new Column(name, type, assign));
            return this;
        }

        public Schema<Row> build() {
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("schema must have at least one column");
            }
            return new Schema<>(Row.class, columns);
        }
    }
}
