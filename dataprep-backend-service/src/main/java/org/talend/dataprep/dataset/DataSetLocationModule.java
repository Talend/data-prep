package org.talend.dataprep.dataset;

import static java.util.Collections.emptyList;

import java.util.List;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.json.DataSetLocationMapping;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A Jackson module that gathers all declared {@link DataSetLocationMapping} found in the current
 * context and registers them as a sub-type of {@link DataSetLocation}.
 *
 * Used for JSON unmarshalling as a replacement of the {@link com.fasterxml.jackson.annotation.JsonSubTypes}
 * annotation on {@link DataSetLocation}.
 */
@Component
public class DataSetLocationModule extends SimpleModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetLocationModule.class);

    @Autowired(required = false)
    private List<DataSetLocationMapping> mappings = emptyList();

    @PostConstruct
    public void init(){
        mappings.forEach(mapping -> registerLocationMapping(mapping.getLocationType(), mapping.getLocationClass()));
    }

    protected void registerLocationMapping(String type, Class<? extends DataSetLocation> locationClass){
        LOGGER.debug("register dataset location type [{}] for [{}]", type, locationClass);
        this.registerSubtypes(new NamedType(locationClass, type));
    }
}
