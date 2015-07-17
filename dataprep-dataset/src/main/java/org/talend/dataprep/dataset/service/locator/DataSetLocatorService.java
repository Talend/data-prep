package org.talend.dataprep.dataset.service.locator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;

/**
 * Service that retrieve dataset location from dataset locators.
 * 
 * @see DataSetLocator
 */
@Service
public class DataSetLocatorService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DataSetLocatorService.class);

    /** Available locators. */
    @Autowired
    private List<DataSetLocator> locators;

    /**
     * Return the dataset location from the content type and the connection parameters.
     *
     * @param contentType the dataset content type.
     * @param connectionParameters the connection parameters in json.
     * @return the dataset location.
     * @throws IOException if there's an error reading dataset location.
     */
    public DataSetLocation getDataSetLocation(String contentType, InputStream connectionParameters) throws IOException {

        DataSetLocation location = null;

        // go through all dataset locator until one is able to retrieve the location
        for (DataSetLocator locator : locators) {
            if (locator.accept(contentType)) {
                location = locator.getLocation(connectionParameters);
                break;
            }
        }

        // local store location as fallback / default
        if (location == null) {
            location = new LocalStoreLocation();
        }

        LOG.debug("Location is {} for content type {}", location, contentType);

        return location;
    }
}
