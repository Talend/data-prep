package org.talend.dataprep.dataset.client;

import java.util.List;

import org.talend.dataprep.dataset.client.domain.Dataset;

import com.fasterxml.jackson.databind.node.ObjectNode;

// mimicing spring Crudrepository
public interface DatasetClient {

    Dataset findOne(String datasetId);

    ObjectNode findSample(String datasetId, int offset, int size);

    List<Dataset> findAll();

    boolean exists(String id);

    long count();

    void delete(String id);

    void delete(Dataset entity);
}
