// ============================================================================
//
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

package org.talend.dataprep.schema;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
     * @see org.springframework.context.ApplicationContext#getBean(String)
     */
    SchemaParser getSchemaGuesser();

    /**
     * @return {@link org.talend.dataprep.schema.Serializer serializer} able to transform the underlying data set
     * content into JSON stream.
     * @see org.springframework.context.ApplicationContext#getBean(String)
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

    @Component
    class Factory {

        @Autowired
        private Map<String, FormatFamily> formatFamilyMap;

        public FormatFamily getFormatFamily(String formatGuessId) {
            return formatFamilyMap.get(formatGuessId);
        }

        public boolean hasFormatFamily(String formatGuessId) {
            return formatFamilyMap.containsKey(formatGuessId);
        }

    }

}
