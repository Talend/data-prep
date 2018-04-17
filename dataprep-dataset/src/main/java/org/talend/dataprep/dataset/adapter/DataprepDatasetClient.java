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

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.client.DatasetClient;
import org.talend.dataprep.dataset.client.domain.Dataset;
import org.talend.dataprep.dataset.client.domain.EncodedSample;
import org.talend.dataprep.dataset.service.DataSetService;

/**
 * Dataprep implementation of {@link DatasetClient}
 */
public class DataprepDatasetClient implements DatasetClient {

    private final DataSetService dataSetService;

    private final BeanConversionService beanConversionService;

    public DataprepDatasetClient(DataSetService dataSetService, BeanConversionService beanConversionService) {
        this.dataSetService = dataSetService;
        this.beanConversionService = beanConversionService;
    }

    @Override
    public Dataset findOne(String datasetId) {
        DataSet datasetMetadata = dataSetService.getMetadata(datasetId);
        DataSetMetadata metadata = datasetMetadata.getMetadata();

        return beanConversionService.convert(metadata, Dataset.class);
    }

    @Override
    public EncodedSample findSample(String datasetId, PageRequest pageRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Dataset> findAll() {
        return null;
    }

    @Override
    public boolean exists(String id) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void delete(Dataset entity) {

    }
}
