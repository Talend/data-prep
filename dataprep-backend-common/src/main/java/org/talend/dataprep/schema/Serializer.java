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

package org.talend.dataprep.schema;

import java.io.InputStream;

import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Represents a class able to serialize a data set content into JSON.
 */
public interface Serializer {

    /**
     * Process <code>rawContent</code> and returns a {@link java.io.InputStream} to the JSON output.
     * 
     * @param rawContent The data set content to process.
     * @param metadata Data set metadata (use it for column names).
     * @param limit A limit for the serialize (pass -1 for "no limit").
     * @return A {@link java.io.InputStream} to the JSON transformation of the <code>rawContent</code>.
     */
    InputStream serialize(InputStream rawContent, DataSetMetadata metadata, long limit);
}
