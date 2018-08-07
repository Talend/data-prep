package org.talend.dataprep.dataset.adapter;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public interface DataCatalog {

    Stream<Dataset> listDataset(Dataset.CertificationState certification, Boolean favorite);

    @Nullable
    Dataset getMetadata(String id);

    @Nullable
    Schema getDataSetSchema(String id);

    Stream<GenericRecord> getDataSetContent(String id, Long limit);
}
