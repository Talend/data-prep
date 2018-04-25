package org.talend.dataprep.dataset.adapter;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetContent;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadata;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetSchema;
import org.talend.dataprep.util.avro.AvroUtils;

import java.util.stream.Stream;

/**
 * Client based on Hystrix commands to call a dataset API. Exposes native avro calls and conversions.
 */
// It also allows to avoid using context.getBean everywhere
@Service
public class ApiDatasetClient {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSetMetadataBuilder dataSetMetadataBuilder;

    public Dataset getMetadata(String id) {
        return context.getBean(DataSetGetMetadata.class, id).execute();
    }

    public DataSetMetadata getDataSetMetadata(String id) {
        Dataset dataset = getMetadata(id);
        RowMetadata rowMetadata = getDataSetRowMetadata(id);

        DataSetMetadata metadata = dataSetMetadataBuilder.metadata() //
                .id(dataset.getId()) //
                .name(dataset.getLabel()) //
                .created(dataset.getCreated()) //
                .modified(dataset.getUpdated()) //
                .build();
        metadata.setRowMetadata(rowMetadata);
        return metadata;
    }

    public Schema getDataSetSchema(String id) {
        return context.getBean(DataSetGetSchema.class, id).execute();
    }

    public RowMetadata getDataSetRowMetadata(String id) {
        Schema dataSetSchema = getDataSetSchema(id);
        return AvroUtils.toRowMetadata(dataSetSchema);
    }

    public Stream<GenericRecord> getDataSetContent(String id) {
        Schema schema = getDataSetSchema(id);
        return context.getBean(DataSetGetContent.class, id, schema).execute();
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, RowMetadata rowMetadata) {
        return getDataSetContent(id).map(AvroUtils.toDataSetRowConverter(rowMetadata));
    }

    public DataSet getDataSet(String id) {
        DataSet dataset = new DataSet();
        DataSetMetadata dataSetMetadata = getDataSetMetadata(id);
        dataset.setMetadata(dataSetMetadata);
        dataset.setRecords(getDataSetContentAsRows(id, dataSetMetadata.getRowMetadata()));
        return dataset;
    }

}
