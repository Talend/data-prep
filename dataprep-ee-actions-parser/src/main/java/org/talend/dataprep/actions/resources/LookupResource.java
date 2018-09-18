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

package org.talend.dataprep.actions.resources;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.transformation.actions.datablending.LookupDatasetsManager;

public class LookupResource implements FunctionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LookupResource.class);

    private final Map<String, LightweightExportableDataSet> lookupDataSets;

    public LookupResource(Map<String, LightweightExportableDataSet> lookupDataSets) {
        this.lookupDataSets = lookupDataSets;
    }

    @Override
    public void register() {
        LOGGER.debug("Adding cached data sets to LookupDataSetManager");
        lookupDataSets.entrySet().forEach(entry -> {
            if (LookupDatasetsManager.put(entry.getKey(), entry.getValue())) {
                LOGGER.debug("Added {} to the lookup data sets", entry.getKey());
            }
        });
    }
}
