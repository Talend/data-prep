// ============================================================================
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

package org.talend.dataprep.transformation.actions.datablending;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;

public class LookupDatasetsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LookupDatasetsManager.class);

    private final Map<String, LightweightExportableDataSet> datasets;

    private static LookupDatasetsManager uniqueInstance;

    private LookupDatasetsManager() {
        datasets = new HashMap<>();
    }

    static {
        LOGGER.info("Creating the unique instance of LookupDatasetManager");
        if (uniqueInstance == null) {
            uniqueInstance = new LookupDatasetsManager();
        }
    }

    private Map<String, LightweightExportableDataSet> getDatasets() {
        return datasets;
    }

    public static synchronized boolean put(String dataSetId, LightweightExportableDataSet dataSet) {
        if (uniqueInstance.getDatasets().containsKey(dataSetId)) {
            LOGGER.debug("The DATASET of id: " + dataSetId + " has already been added");
            return false;
        } else {
            LOGGER.debug("Adding the DATASET of id: " + dataSetId);
            uniqueInstance.getDatasets().put(dataSetId, dataSet);
            return true;
        }
    }

    public static synchronized LightweightExportableDataSet remove(String dataSetId) {
        LOGGER.debug("removing the DATASET of id: " + dataSetId);
        return uniqueInstance.getDatasets().remove(dataSetId);
    }

    public static LightweightExportableDataSet get(String dataSetId) {
        LOGGER.debug("Retrieving the DATASET of id: " + dataSetId);
        return uniqueInstance.getDatasets().get(dataSetId);
    }
}
