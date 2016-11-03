//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset.location.locator;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.location.HdfsLocation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dataset locator for remote hdfs datasets.
 */
@Component
public class HdfsDataSetLocator implements DataSetLocator {

    /** DataSet media type for remote hdfs datasets. */
    public static final String MEDIA_TYPE = "application/vnd.remote-ds.hdfs";

    /** Jackson builder. */
    @Autowired
    @Lazy
    private ObjectMapper objectMapper;

    /**
     * @see DataSetLocator#accept(String)
     */
    @Override
    public boolean accept(String contentType) {
        return StringUtils.startsWith(contentType, MEDIA_TYPE);
    }

    /**
     * @see DataSetLocator#getLocation(InputStream)
     */
    @Override
    public DataSetLocation getLocation(InputStream connectionParameters) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(connectionParameters);
        return objectMapper.readerFor(DataSetLocation.class).readValue(parser);
    }

    @Override
    public String getLocationType() {
        return HdfsLocation.NAME;
    }

    @Override
    public Class<? extends DataSetLocation> getLocationClass() {
        return HdfsLocation.class;
    }
}
