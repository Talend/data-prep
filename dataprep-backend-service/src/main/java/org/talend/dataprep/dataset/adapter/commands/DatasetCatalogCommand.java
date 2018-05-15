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

package org.talend.dataprep.dataset.adapter.commands;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.CatalogConfiguration;

import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * provide dataset url regarding dataset provider configuration
 * @see org.talend.dataprep.dataset.DatasetConfiguration
 * @see CatalogConfiguration
 * @param <T>
 */
public abstract class DatasetCatalogCommand<T> extends GenericCommand<T> {

    @Autowired(required = false)
    private CatalogConfiguration catalogConfiguration;

    private String datasetUrl;

    protected DatasetCatalogCommand(HystrixCommandGroupKey group) {
        super(group);
    }

    @PostConstruct
    public void init() {
        if (catalogConfiguration != null) {
            this.datasetUrl = catalogConfiguration.getService().getUrl();
        } else {
            this.datasetUrl = datasetServiceUrl;
        }
    }

    protected String getDatasetUrl() {
        return datasetUrl;
    }
}
