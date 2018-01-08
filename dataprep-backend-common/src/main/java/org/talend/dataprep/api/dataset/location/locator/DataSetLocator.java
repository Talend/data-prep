// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.dataset.location.locator;

import java.io.IOException;
import java.io.InputStream;

import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.json.DataSetLocationMapping;

/**
 * Interface for dataset locators.
 */
public interface DataSetLocator extends DataSetLocationMapping {

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
