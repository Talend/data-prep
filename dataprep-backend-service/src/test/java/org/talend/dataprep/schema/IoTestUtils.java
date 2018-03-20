//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.schema.csv.CSVFormatFamily;

/**
 * Utility class for the io unit tests.
 */
@Component
public class IoTestUtils {

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /**
     * Default constructor.
     */
    private IoTestUtils() {
        // private constructor for utility class
    }

    /**
     * @return a ready to use dataset.
     */
    public DataSetMetadata getDataSetMetadata() {
        DataSetMetadata datasetMetadata = metadataBuilder.metadata().id("123456789").build();
        datasetMetadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        return datasetMetadata;
    }

    /**
     * @param id the column id.
     * @param name the column name.
     * @return the wanted column metadata.
     */
    public ColumnMetadata getColumnMetadata(int id, String name) {
        return ColumnMetadata.Builder.column().id(id).name(name).type(Type.STRING).build();
    }

    /**
     * @param wantedColumns the wanted columns for the dataset.
     * @return a simple dataset metadata with as many columns as wanted
     */
    public DataSetMetadata getSimpleDataSetMetadata(String... wantedColumns) {

        List<ColumnMetadata> columns = new ArrayList<>(wantedColumns.length);
        for (int i = 0; i < wantedColumns.length; i++) {
            columns.add(getColumnMetadata(i, wantedColumns[i]));
        }

        DataSetMetadata datasetMetadata = getDataSetMetadata();
        datasetMetadata.setRowMetadata(new RowMetadata((columns)));

        return datasetMetadata;

    }
}
