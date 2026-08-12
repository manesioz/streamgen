package io.github.manesioz.streamgen.model;

/**
 * Keyed, event-timed record for DataStream labs.
 *
 * <p>Mutable JavaBean shape so Flink {@code Types.POJO} works. {@code value} may be
 * {@code null} when a null-rate policy applies.
 */
public class KeyedTimedRecord {

    private String key;
    private String value;
    private long timestamp;

    public KeyedTimedRecord() {}

    public KeyedTimedRecord(String key, String value, long timestamp) {
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "KeyedTimedRecord{key='" + key + "', value='" + value + "', timestamp=" + timestamp + '}';
    }
}
