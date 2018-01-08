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

package org.talend.dataprep.api.dataset;

import java.util.stream.Stream;

import org.talend.dataprep.api.dataset.json.DataSetRowStreamDeserializer;
import org.talend.dataprep.api.dataset.json.DataSetRowStreamSerializer;
import org.talend.dataprep.api.dataset.row.DataSetRow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonRootName("dataset")
public class DataSet {

    @JsonProperty(value = "metadata")
    private DataSetMetadata metadata;

    @JsonProperty(value = "records")
    @JsonSerialize(using = DataSetRowStreamSerializer.class)
    @JsonDeserialize(using = DataSetRowStreamDeserializer.class)
    private Stream<DataSetRow> records;

    public static DataSet empty() {
        DataSet dataSet = new DataSet();
        dataSet.setRecords(Stream.empty());
        return dataSet;
    }

    public DataSetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DataSetMetadata metadata) {
        this.metadata = metadata;
    }

    public Stream<DataSetRow> getRecords() {
        return records;
    }

    public void setRecords(Stream<DataSetRow> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "DataSet{" + "metadata=" + metadata + ", records=...}";
    }
}
