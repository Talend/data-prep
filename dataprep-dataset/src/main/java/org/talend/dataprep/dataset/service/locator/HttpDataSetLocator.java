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

package org.talend.dataprep.dataset.service.locator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.location.HttpLocation;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dataset locator for remote http datasets.
 */
@Component
public class HttpDataSetLocator implements DataSetLocator {

    /** Jackson builder. */
    @Autowired
    @Lazy
    private ObjectMapper objectMapper;

    /**
     * @see DataSetLocator#accept(String)
     */
    @Override
    public boolean accept(String contentType) {
        return StringUtils.startsWith(contentType, HttpLocation.MEDIA_TYPE);
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
        return HttpLocation.NAME;
    }

    @Override
    public Class<? extends DataSetLocation> getLocationClass() {
        return HttpLocation.class;
    }
}
