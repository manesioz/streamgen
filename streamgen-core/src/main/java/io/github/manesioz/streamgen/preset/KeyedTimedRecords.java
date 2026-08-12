package io.github.manesioz.streamgen.preset;

import io.github.manesioz.streamgen.model.KeyedTimedRecord;
import io.github.manesioz.streamgen.row.Row;

/** Binds {@link Row} values from {@link Schemas#keyedTimed} to {@link KeyedTimedRecord}. */
public final class KeyedTimedRecords {

    private KeyedTimedRecords() {}

    public static KeyedTimedRecord fromRow(Row row) {
        return new KeyedTimedRecord(
                row.getString(Schemas.KEY),
                row.getString(Schemas.VALUE),
                row.getLong(Schemas.TIMESTAMP));
    }
}
