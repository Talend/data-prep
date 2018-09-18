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

package org.talend.dataprep;

import java.util.Collections;
import java.util.Map;

import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataquality.semantic.broadcast.TdqCategories;

public class StandalonePreparation extends PreparationMessage {

    private TdqCategories tdqCategories;

    private Map<String, LightweightExportableDataSet> lookupDataSets = Collections.emptyMap();

    private String filterOut;

    public TdqCategories getTdqCategories() {
        return tdqCategories;
    }

    public void setTdqCategories(TdqCategories tdqCategories) {
        this.tdqCategories = tdqCategories;
    }

    public Map<String, LightweightExportableDataSet> getLookupDataSets() {
        return lookupDataSets;
    }

    public void setLookupDataSets(Map<String, LightweightExportableDataSet> lookupDataSets) {
        this.lookupDataSets = lookupDataSets;
    }

    public String getFilterOut() {
        return filterOut;
    }

    public void setFilterOut(String filterOut) {
        this.filterOut = filterOut;
    }
}
