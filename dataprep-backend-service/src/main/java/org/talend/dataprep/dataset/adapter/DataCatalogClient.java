package org.talend.dataprep.dataset.adapter;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetContent;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadata;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetSchema;
import org.talend.dataprep.dataset.adapter.commands.DatasetList;

/**
 * Client for catalog HTTP API.
 */
@Service
public class DataCatalogClient {

    @Autowired
    private ApplicationContext context;

    // ------- Pure API -------

    public Stream<Dataset> listDataset(Dataset.CertificationState certification, Boolean favorite) {
        return context.getBean(DatasetList.class, certification, favorite).execute();
    }

    @Nullable
    public Dataset getMetadata(String id) {
        return context.getBean(DataSetGetMetadata.class, id).execute();
    }

    public Schema getDataSetSchema(String id) {
        return context.getBean(DataSetGetSchema.class, id).execute();
    }

    public Stream<GenericRecord> getDataSetContent(String id, Long limit, Schema schema) {
        return context.getBean(DataSetGetContent.class, id, schema, limit).execute();
    }

}
