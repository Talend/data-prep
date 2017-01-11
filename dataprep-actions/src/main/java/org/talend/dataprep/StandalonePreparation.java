// ============================================================================
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

package org.talend.dataprep;

import java.util.List;
import java.util.Map;

import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataquality.semantic.broadcast.BroadcastDocumentObject;

public class StandalonePreparation extends PreparationMessage {

    private List<BroadcastDocumentObject> dictionary;

    private List<BroadcastDocumentObject> keyword;

    private Map<String, LightweightExportableDataSet> lookupDataSets;

    public List<BroadcastDocumentObject> getDictionary() {
        return dictionary;
    }

    public void setDictionary(List<BroadcastDocumentObject> dictionary) {
        this.dictionary = dictionary;
    }

    public List<BroadcastDocumentObject> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<BroadcastDocumentObject> keyword) {
        this.keyword = keyword;
    }

    public Map<String, LightweightExportableDataSet> getLookupDataSets() {
        return lookupDataSets;
    }

    public void setLookupDataSets(Map<String, LightweightExportableDataSet> lookupDataSets) {
        this.lookupDataSets = lookupDataSets;
    }
}
