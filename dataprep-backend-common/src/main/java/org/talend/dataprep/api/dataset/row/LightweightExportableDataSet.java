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

package org.talend.dataprep.api.dataset.row;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.talend.dataprep.api.dataset.RowMetadata;

public class LightweightExportableDataSet implements Serializable {

    private RowMetadata metadata;


    private Map<String, Map<String, String>> records;


    public LightweightExportableDataSet() {
    }

    public RowMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(RowMetadata metadata) {
        this.metadata = metadata;
    }

    public LightweightExportableDataSet(RowMetadata metadata, Map<String, Map<String, String>> values) {
        this.metadata = metadata;
        this.records = values;
    }

    public Map<String, Map<String, String>> getRecords() {
        return records;
    }

    public void setRecords(Map<String, Map<String, String>> records) {
        this.records = records;
    }

    public Map<String, String> addRecord(String key, Map<String, String> values) {
        if (MapUtils.isEmpty(records)) {
            records = new HashMap<>();
        }
        return records.put(key, values);
    }

    public boolean isEmpty() {
        return MapUtils.isEmpty(records);
    }

    public int size() {
        return MapUtils.isEmpty(records) ? 0 : records.size();
    }
}
