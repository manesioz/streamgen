package io.github.manesioz.streamgen.row;

import io.github.manesioz.streamgen.Boundedness;
import io.github.manesioz.streamgen.schema.Field;
import io.github.manesioz.streamgen.schema.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Pure {@code index → Row} function. Safe to call from unit tests, MiniCluster
 * {@code env.fromData(...)}, or Flink's {@code GeneratorFunction}.
 */
public final class IndexRowGenerator implements Serializable {

    private final Schema schema;
    private final String[] names;

    public IndexRowGenerator(Schema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.names = schema.names();
    }

    public Schema schema() {
        return schema;
    }

    public Row map(long index) {
        Object[] values = new Object[schema.size()];
        List<Field> fields = schema.fields();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (field.nullRate().shouldNull(index)) {
                values[i] = null;
            } else {
                values[i] = field.policy().assign(index);
            }
        }
        return new Row(names, values);
    }

    /** Materialize {@code [0, count)} for {@code env.fromData} style tests. */
    public List<Row> list(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("list() cannot materialize more than Integer.MAX_VALUE rows");
        }
        List<Row> rows = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            rows.add(map(i));
        }
        return rows;
    }

    public List<Row> list(Boundedness boundedness) {
        if (!boundedness.isBounded()) {
            throw new IllegalArgumentException("list() requires finite boundedness");
        }
        return list(boundedness.count());
    }

    public Stream<Row> stream(long count) {
        return LongStream.range(0, count).mapToObj(this::map);
    }
}
