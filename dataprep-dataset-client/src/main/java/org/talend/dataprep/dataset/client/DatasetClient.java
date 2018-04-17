package org.talend.dataprep.dataset.client;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.talend.dataprep.dataset.client.domain.Dataset;
import org.talend.dataprep.dataset.client.domain.EncodedSample;

// mimicing spring Crudrepository
public interface DatasetClient {

    Dataset findOne(String datasetId);

    EncodedSample findSample(String datasetId, PageRequest pageRequest);

    List<Dataset> findAll();

    boolean exists(String id);

    long count();

    void delete(String id);

    void delete(Dataset entity);
}
