# streamgen

Deterministic synthetic events for Flink tests. Same index in, same event out.

The only contract is `Assign<V>`: `long index -> V`, pure, `Serializable`. Your event's constructor is the schema; the library never reflects on your type.

```java
record Order(String customerId, long ts, Status status, double amount) {}

Distribution customers = Distribution.hot(10_000, 80);           // 80% of traffic on 1% of keys

Assign<String> customerId = customers.prefixed("u-");
Assign<Long>   ts         = Clock.outOfOrder(Clock.stepping(T0, 1_000), 30_000);
Assign<Status> status     = Distribution.uniform(3).pick(List.of(Status.values()));
Assign<Double> amount     = Values.range(1, 500);

Assign<Order> orders = i -> new Order(
        customerId.assign(i), ts.assign(i), status.assign(i), amount.assign(i));

Assign<Order> stream = Multiplicity.duplicates(orders, 100);      // every 100th row repeats the previous

DataGeneratorSource<Order> source = Streamgen.source(stream, 1_000_000, Order.class);
```

Behaviors are grouped by the stream property they model:

| | |
|---|---|
| `Distribution` | how an index lands on a domain of `size()` slots: `uniform`, `hot`. Reuse one across columns for consistent skew. |
| `Clock` | how event time advances: `stepping`, `outOfOrder`, `late`. Decorators over `Assign<Long>`. |
| `Multiplicity` | whether an index repeats another row: `duplicates`. Decorates the whole `Assign<T>`. |
| `Values` | boring `long -> V` mappings: `index`, `prefix`, `range`. |

`streamgen-core` has no Flink dependency. `streamgen-flink` adapts an `Assign<T>` to `DataGeneratorSource` (Flink 2.3, provided). Java 17.

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
