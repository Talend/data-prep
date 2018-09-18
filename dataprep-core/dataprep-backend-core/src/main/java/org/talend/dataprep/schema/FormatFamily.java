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

import org.talend.dataprep.api.dataset.ColumnMetadata;

/**
 * Represents a Format family (CSV, XLS, HTML) for a data set content format.
 */
public interface FormatFamily {

    /**
     * @return The MIME type of the format guess.
     */
    String getMediaType();

    /**
     * @return {@link SchemaParser} that allowed data prep to read {@link ColumnMetadata column metadata} information
     * from the data set.
     */
    SchemaParser getSchemaGuesser();

    /**
     * @return {@link org.talend.dataprep.schema.Serializer serializer} able to transform the underlying data set
     * content into JSON stream.
     */
    Serializer getSerializer();

    /**
     *
     * @return the Spring beanId to be used to get the bean from the used injection container
     */
    String getBeanId();

    /**
     *
     * @return {@link DraftValidator} that will validate if the metadata are still in draft status for this format type
     */
    DraftValidator getDraftValidator();

}
