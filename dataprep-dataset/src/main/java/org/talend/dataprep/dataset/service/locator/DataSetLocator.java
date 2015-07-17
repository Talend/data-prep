package org.talend.dataprep.dataset.service.locator;

import java.io.IOException;
import java.io.InputStream;

import org.talend.dataprep.api.dataset.DataSetLocation;

/**
 * Interface for dataset locators.
 */
public interface DataSetLocator {

    /**
     * @param contentType the content type to analyse.
     * @return true if the locator can deal with this content type.
     */
    boolean accept(String contentType);

    /**
     * @param location the connection parameters to parse json for this location.
     * @return the dataset location.
     */
    DataSetLocation getLocation(InputStream location) throws IOException;

}
