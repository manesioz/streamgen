# streamgen

Deterministic, composable synthetic events for stream-processing tests.

`assign(index)` is a **pure function of `(index, policy config)`**. Restore, replay, and parallel splits of the index space produce the same rows. That is the contract. Everything else is composition.

## Modules

| Artifact | Depends on Flink? | Use it for |
|---|---|---|
| `streamgen-core` | No | Schemas, field policies, `IndexRowGenerator.list(n)` in unit tests |
| `streamgen-flink` | Yes (provided) | `FlinkDataGen` → `DataGeneratorSource<Row>` |

Group id: `io.github.manesioz`. Version: `0.1.0-SNAPSHOT`.

```xml
<dependency>
  <groupId>io.github.manesioz</groupId>
  <artifactId>streamgen-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Install locally with `mvn install`. Publishing to GitHub Packages can come later.

## Three ways in

**1. Preset**

```java
Schema schema = Schemas.keyedTimed(1_000, 1_700_000_000_000L, 1_000L);
List<Row> rows = new IndexRowGenerator(schema).list(10_000);
```

**2. Compose policies**

```java
Schema schema = Schema.builder()
    .field("device_id", FieldType.STRING, Keying.hot(10_000, 0.8))
    .field("event_id", FieldType.STRING, Policies.duplicate(Payload.sequence("evt-"), 5, 3))
    .field("ts", FieldType.BIGINT, Time.delayed(1_700_000_000_000L, 1_000L, 30_000L))
    .field("speed", FieldType.DOUBLE, Payload.seededDoubleRange(7L, 0.0, 120.0), NullRate.of(0.02))
    .build();
```

**3. Custom policy**

```java
FieldPolicy vin = index -> "1HGCM" + (index % 50_000);
// or decorate:
FieldPolicy tagged = Payload.sequence("v-").andThen((index, value) -> value + "-x");
```

## MiniCluster / `fromData` tests

Terminal jobs often override the source with `env.fromData(List<...>)`. Materialize in core, map to bytes (or POJOs) in the test:

```java
List<Row> rows = new IndexRowGenerator(schema).list(200);
List<byte[]> payloads = rows.stream().map(this::toProtoBytes).toList();
env.fromData(payloads);
```

Live Flink source when you want rate limiting:

```java
DataGeneratorSource<Row> source = FlinkDataGen.source(schema)
    .finite(10_000)
    .rate(RateLimiterStrategy.perSecond(1_000))
    .build();
```

Jobs own `WatermarkStrategy`. streamgen does not.

## Combinators (v0)

| Combinator | What it models |
|---|---|
| `Keying.uniform` / `Keying.hot` | Cardinality and skew |
| `Time.monotonic` / `Time.delayed` | Event time, late arrivals |
| `Policies.duplicate` | Retransmits / fingerprint collisions |
| `Policies.mix` | Two populations on one field |
| `Policies.jitter` | Lag on a BIGINT clock |
| `NullRate` | Sparse payload fields |
| `FieldPolicy.andThen` | Escape hatch |

## Non-goals (v0)

- Watermark strategies
- Multi-stream correlation DSL
- Schema-drift engine
- Chaos Mesh / cluster failure injection
- Exhaustive scenario YAML

## Layout

```
streamgen-core/     # zero Flink deps
streamgen-flink/    # DataGeneratorSource adapter
```

Java 17. Flink 2.3.0 for the adapter (provided scope).

## Build

```bash
mvn test
```
