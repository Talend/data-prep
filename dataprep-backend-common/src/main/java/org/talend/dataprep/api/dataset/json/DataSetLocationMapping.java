package org.talend.dataprep.api.dataset.json;

import java.io.Serializable;

import org.talend.dataprep.api.dataset.DataSetLocation;

/**
 * Declares a mapping between a {@link DataSetLocation} class and its JSON type.
 *
 * This is used for JSON marshalling / unmarshalling of {@link DataSetLocation} implementations
 *
 */
public interface DataSetLocationMapping extends Serializable {

    /**
     * @return the string identifying the {@link DataSetLocation} sub-type
     */
    String getLocationType();

    /**
     * @return the corresponding {@link DataSetLocation} implementation.
     */
    Class<? extends DataSetLocation> getLocationClass();

}
