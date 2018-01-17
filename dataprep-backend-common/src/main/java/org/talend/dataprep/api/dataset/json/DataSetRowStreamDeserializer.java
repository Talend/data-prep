// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.dataset.json;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DataSetRowStreamDeserializer extends JsonDeserializer<Stream<DataSetRow>> {

    @Override
    public Stream<DataSetRow> deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        final List<ColumnMetadata> columns = (List<ColumnMetadata>) context
                .getAttribute(ColumnContextDeserializer.class.getName());
        final RowMetadata rowMetadata;
        if (columns == null) {
            rowMetadata = new RowMetadata();
        } else {
            rowMetadata = new RowMetadata(columns);
        }
        final Iterable<DataSetRow> rowIterable = () -> new DataSetRowIterator(jp, rowMetadata);
        return StreamSupport.stream(rowIterable.spliterator(), false);
    }
}
