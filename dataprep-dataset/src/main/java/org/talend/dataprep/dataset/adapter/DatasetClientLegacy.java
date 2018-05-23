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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;

/**
 * Dataset legacy implementation of {@link DatasetClientLegacy}
 */
@Service
public class DatasetClientLegacy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetClientLegacy.class);

    private final DataSetService dataSetService;

    private final BeanConversionService beanConversionService;

    public DatasetClientLegacy(DataSetService dataSetService, BeanConversionService beanConversionService) {
        this.dataSetService = dataSetService;
        this.beanConversionService = beanConversionService;
    }

    public boolean exists(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public long count() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void delete(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void delete(Dataset entity) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
