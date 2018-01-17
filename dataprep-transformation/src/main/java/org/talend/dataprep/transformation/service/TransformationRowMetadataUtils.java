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

package org.talend.dataprep.transformation.service;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;

/**
 * Utility class for the transformation module to deal with rowMetadata.
 */
@Component
public class TransformationRowMetadataUtils {

    /**
     * Return an empty RowMetadata out of the given one.
     *
     * @param metadata the metadata to start from.
     * @return an empty RowMetadata out of the given one.
     */
    public RowMetadata getMatchingEmptyRowMetadata(RowMetadata metadata) {
        RowMetadata raw = new RowMetadata();

        metadata.getColumns().forEach(c -> {
            ColumnMetadata rawColumn = ColumnMetadata.Builder.column() //
                    .copyMatchingEmptyColumnMetadata(c) //
                    .build();
            raw.addColumn(rawColumn);
        });

        return raw;
    }

}
