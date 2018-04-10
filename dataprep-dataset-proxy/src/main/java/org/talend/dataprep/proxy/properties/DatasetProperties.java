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

package org.talend.dataprep.proxy.properties;

import java.net.URL;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dataset")
public class DatasetProperties {

    public DatasetProperties() {}

    /**
     * DataSet type: Dataprep Dataset or DataCalog Dataset
     */
    private DataSetType type;

    private URL url;

    public DataSetType getType() {
        return type;
    }

    public void setType(DataSetType type) {
        this.type = type;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public enum DataSetType { LEGACY, CATALOG}
}
