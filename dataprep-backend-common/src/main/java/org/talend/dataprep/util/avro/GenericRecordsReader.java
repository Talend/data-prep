package org.talend.dataprep.util.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class GenericRecordsReader implements Closeable {

    private final AvroReader avroReader;

    private final List<ColumnMetadata> columnsMetadata;

    public GenericRecordsReader(AvroReader avroReader, List<ColumnMetadata> columnsMetadata) {
        this.avroReader = avroReader;
        this.columnsMetadata = columnsMetadata;
    }

    public DataSetRow read() throws IOException {
        if (!avroReader.isClosed()) {
            GenericRecord record = avroReader.next();
            HashMap<String, String> values = new HashMap<>();
            for (ColumnMetadata column : columnsMetadata) {
                values.put(column.getId(), toString(record, column));
            }
            return new DataSetRow(values);
        } else {
            // End of stream
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        avroReader.close();
    }

    private static String toString(GenericRecord currentRecord, ColumnMetadata column) {
        final Schema recordSchema = currentRecord.getSchema().getField(column.getName()).schema();
        final Schema.Type type = recordSchema.getType();

        switch (type) {
        case BYTES:
            return new String(((ByteBuffer) currentRecord.get(column.getName())).array());
        case STRING:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case BOOLEAN:
        case UNION:
        case ENUM:
            return String.valueOf(currentRecord.get(column.getName()));
        case NULL:
            return "";
        default: // RECORD, ARRAY, MAP, FIXED
            return "Data Preparation cannot interpret this value";
        }
    }

    public boolean isClosed() {
        return avroReader.isClosed();
    }
}
