package org.talend.dataprep.dataset.service.locator;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.location.HttpLocation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dataset locator for remote http datasets.
 */
@Component
public class HttpDataSetLocator implements DataSetLocator {

    /** DataSet media type for remote http datasets. */
    public static final String MEDIA_TYPE = "application/vnd.remote-ds.http";

    /** Jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    /**
     * @see DataSetLocator#accept(String)
     */
    @Override
    public boolean accept(String contentType) {
        return StringUtils.equalsIgnoreCase(MEDIA_TYPE, contentType);
    }

    /**
     * @see DataSetLocator#getLocation(InputStream)
     */
    @Override
    public DataSetLocation getLocation(InputStream connectionParameters) throws IOException {
        ObjectMapper mapper = builder.build();
        JsonParser parser = mapper.getFactory().createParser(connectionParameters);
        HttpLocation location = mapper.reader(DataSetLocation.class).readValue(parser);
        return location;
    }

}
