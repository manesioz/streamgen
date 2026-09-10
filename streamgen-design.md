# Streamgen

A small Java layer on Flink’s `DataGeneratorSource`. The source already maps `0..N` to events. Streamgen makes that mapping a schema of named `assign(index)` functions instead of one-off lambdas.

DataStream only. Not a Table connector, not a faker, not a test harness.

## Contract

```text
index  →  assign (per field)  →  record T  →  GeneratorFunction adapter
```

Same index, same value. No `Random`, no wall clock, no counters. A subtask only sees some indexes; the index *is* the state.

Flink owns how many records, rate, parallelism, and splits. Streamgen owns `index → T`.

### 1. `assign(index)`

```java
interface Assign<V> {
    V assign(long index);
}
```

A field is a name plus an `Assign`. Type, if present, only checks what `assign` returns. There is no default `assign` from a datatype.

### 2. Schema of named assigns

```java
Schema schema = Schema.of(
    field("userId", index -> "user-" + (index % 1000)),
    field("amount", index -> index % 50),
    field("ts",     index -> 1_700_000_000_000L + index * 1000)
);
```

No required `key` / `time` / `payload`. A one-field schema is valid. So is a `String` with a single `Assign` and no schema.

### 3. `index → T`

Apply every field’s `assign` at that index. `T` is whatever the user wants:

- the record is a `Row` (name → value), or
- the user maps that `Row` to a POJO / bytes / tuple.

Streamgen does not reflect on classes or invent generators from Java types.

### 4. Adapter

```java
GeneratorFunction<Long, T> fn = Streamgen.generator(schema, row -> toOrder(row));

DataGeneratorSource<T> source =
    new DataGeneratorSource<>(fn, 10_000, typeInfo);
```

Optional helpers: `list(n)` for `env.fromData`, or a one-liner that builds the `DataGeneratorSource`. Watermarks stay on the job.

## What this excludes

These look like stream features. They are either more `assign`s, or they do not fit `map(index)`:

| Idea | Where it belongs |
|---|---|
| Hot keys, catalogs, clocks | An `assign` (`index % n`, `start + index * step`, …) |
| Duplicates | `assign` of an earlier index, not a buffer |
| Late / out-of-order time | Jitter in the time field’s `assign`; watermarks on the job |
| Rate, idle, bursts | `DataGeneratorSource` / a custom source — not `assign` |
| Realistic names | Optional later, seeded by **index**, not a live `Random` |

A wrapper around `GeneratorFunction` that sleeps, idles, or mutates a `Random` is a different library.

## Later, if needed

Named `Assign` helpers (`mod`, sequence, monotonic time). DataFaker behind `assign` with the index as seed. Nothing else until this core is obvious in code.
