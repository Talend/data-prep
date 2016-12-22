// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.dataset.location;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetLocation;

/**
 * Service to use to access the available dataset locations.
 */
@Service
public class DataSetLocationService {

    @Autowired
    private List<DataSetLocation> locations;

    /**
     * @return the available dataset locations.
     */
    public List<DataSetLocation> getAvailableLocations() {
        return locations;
    }

    public DataSetLocation findLocation(String locationType) {
        DataSetLocation matchingDatasetLocation = null;
        if (!StringUtils.isEmpty(locationType)) {
            for (DataSetLocation location : getAvailableLocations()) {
                if (locationType.equals(location.getLocationType())) {
                    matchingDatasetLocation = location;
                    break;
                }
            }
        }
        return matchingDatasetLocation;
    }
}
