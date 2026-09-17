# streamgen

Generate deterministic synthetic events from a schema. Same index, same row.

```java
Schema<Order> schema = Schema.of(Order.class)
        .assign("customerId", Id.hot(10_000, 80, "user-"))
        .assign("ts", EventTime.millis(1_700_000_000_000L, 1_000L).lag(30_000L));

List<Order> rows = schema.list(10_000);

DataGeneratorSource<Order> source = FlinkStreamgen.source(schema, 10_000);
```

`streamgen-core` has no Flink dependency. `streamgen-flink` is a `DataGeneratorSource` adapter (Flink 2.3, provided). Java 17.

```xml
<dependency>
    <groupId>io.streamgen</groupId>
    <artifactId>streamgen-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```bash
mvn test
```
