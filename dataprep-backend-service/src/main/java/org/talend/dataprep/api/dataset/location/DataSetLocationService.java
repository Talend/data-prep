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

package org.talend.dataprep.api.dataset.location;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
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

    @Autowired(required = false)
    private List<DatasetLocationsSupplier> datasetLocationsSuppliers = emptyList();

    /**
     * @return the available dataset locations.
     */
    public List<DataSetLocation> getAvailableLocations() {
        List<DataSetLocation> suppliedLocations = datasetLocationsSuppliers.stream() //
                .flatMap(dls -> dls.getAvailableLocations().stream()) //
                .collect(toList());
        ArrayList<DataSetLocation> allLocations = new ArrayList<>();
        allLocations.addAll(locations);
        allLocations.addAll(suppliedLocations);
        return allLocations;
    }

    /**
     * Find DatasetLocation by {@link DataSetLocation#getLocationType()} from all available.
     *
     * @param locationType the type searched
     * @return the location found or null
     */
    public DataSetLocation findLocation(String locationType) {
        DataSetLocation matchingDatasetLocation = null;
        if (!StringUtils.isEmpty(locationType)) {
            matchingDatasetLocation = findMatchingDataSetLocation(locationType, getAvailableLocations());
            if (matchingDatasetLocation == null) {
                matchingDatasetLocation = findDataSetLocationFromSuppliers(locationType, matchingDatasetLocation);
            }
        }
        return matchingDatasetLocation;
    }

    private DataSetLocation findMatchingDataSetLocation(String locationType, Collection<? extends DataSetLocation> locations) {
        DataSetLocation matchingDatasetLocation = null;
        for (DataSetLocation location : locations) {
            if (locationType.equals(location.getLocationType())) {
                matchingDatasetLocation = location;
                break;
            }
        }
        return matchingDatasetLocation;
    }

    private DataSetLocation findDataSetLocationFromSuppliers(String locationType, DataSetLocation matchingDatasetLocation) {
        for (DatasetLocationsSupplier datasetLocationsSupplier : datasetLocationsSuppliers) {
            matchingDatasetLocation = findMatchingDataSetLocation(locationType,
                    datasetLocationsSupplier.getAvailableLocations());
            if (matchingDatasetLocation != null)
                break;
        }
        return matchingDatasetLocation;
    }
}
