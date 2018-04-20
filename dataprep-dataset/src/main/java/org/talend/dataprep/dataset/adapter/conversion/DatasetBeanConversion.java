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

package org.talend.dataprep.dataset.adapter.conversion;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

@Component
public class DatasetBeanConversion extends BeanConversionServiceWrapper {

    @Override
    public BeanConversionService doWith(BeanConversionService instance, String beanName,
            ApplicationContext applicationContext) {
        instance.register(fromBean(DataSetMetadata.class) //
                .toBeans(Dataset.class) //
                .using(Dataset.class, (dataSetMetadata, dataset) -> {
                    dataset.setId(dataSetMetadata.getId());
                    dataset.setEnabled(true);
                    dataset.setCreated(dataSetMetadata.getCreationDate());
                    dataset.setUpdated(dataSetMetadata.getLastModificationDate());
                    dataset.setOwner(dataSetMetadata.getAuthor());
                    dataset.setLabel(dataSetMetadata.getName());
                    DataSetLocation location = dataSetMetadata.getLocation();
                    //TODO fill with TCompLocation instance only
                    // dataset.setProperties();
                    //TODO value according to TCompLocation
                    // dataset.setType();

                    return dataset;
                }).build() //
        ); return instance;
    }
}
