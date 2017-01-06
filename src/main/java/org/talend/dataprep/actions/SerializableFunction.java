// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.actions;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.actions.resources.FunctionResource;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.transformation.pipeline.Pipeline;

class SerializableFunction implements Function<IndexedRecord, IndexedRecord>, Serializable {

    /** For the serialization interface. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SerializableFunction.class);

    private final Pipeline pipeline;

    private final StackedNode stackedNode;

    private final RowMetadata initialRowMetadata;

    private final FunctionResource[] resources;

    private transient boolean loaded = false;

    private transient long count = 0;

    private transient Schema outputSchema;

    SerializableFunction(Pipeline pipeline, StackedNode stackedNode, RowMetadata initialRowMetadata,
            FunctionResource... resources) {
        this.pipeline = pipeline;
        this.stackedNode = stackedNode;
        this.initialRowMetadata = initialRowMetadata;
        this.resources = resources;
    }

    @Override
    public IndexedRecord apply(IndexedRecord indexedRecord) {
        if (!loaded) {
            for (FunctionResource resource : resources) {
                LOG.debug("Loading resource '{}'...", resource);
                resource.register();
            }
            loaded = true;
        }
        if (count++ % 10000 == 0) {
            LOG.debug("Current pipeline state: " + pipeline);
        }

        Map<String, String> values = new HashMap<>();
        final List<Schema.Field> fields = indexedRecord.getSchema().getFields();
        DecimalFormat decimalFormat = new DecimalFormat("0000");
        int i = 0;
        for (Schema.Field field : fields) {
            values.put(decimalFormat.format(i++), String.valueOf(indexedRecord.get(field.pos())));
        }
        final DataSetRow row = new DataSetRow(values);
        pipeline.receive(row, initialRowMetadata);

        // Reapply values of data set row to the indexed record
        final Optional<DataSetRow> result = Optional.ofNullable(stackedNode.pop());
        if (result.isPresent()) {
            final DataSetRow modifiedRow = result.get();
            initializeSchema(modifiedRow);
            GenericRecord modifiedRecord = new GenericData.Record(outputSchema);
            final Iterator<Object> iterator = modifiedRow.order().values().values().iterator();
            for (int j = 0; j < outputSchema.getFields().size() && iterator.hasNext(); j++) {
                modifiedRecord.put(j, iterator.next());
            }
            return modifiedRecord;
        } else {
            return null;
        }
    }

    // Lazy init of result Avro schema for later reuse
    private void initializeSchema(DataSetRow modifiedRow) {
        if (outputSchema == null) {
            // Computing Avro schema is rather long, don't do it for each line.
            final RowMetadata modifiedRowRowMetadata = modifiedRow.getRowMetadata();
            outputSchema = RowMetadataUtils.toSchema(modifiedRowRowMetadata);
        }
    }
}
