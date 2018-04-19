/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.avro.generic.IndexedRecord;
import org.springframework.data.domain.PageRequest;
import org.talend.dataprep.dataset.domain.Dataset;

import java.util.List;
import java.util.stream.Stream;

// mimics spring Crudrepository
public interface DatasetClient {

    Dataset findOne(String datasetId);

    ObjectNode findSchema(String datasetId);

    Stream<IndexedRecord> findData(String datasetId, PageRequest pageRequest);

    List<Dataset> findAll();

    boolean exists(String id);

    long count();

    void delete(String id);

    void delete(Dataset entity);
}
