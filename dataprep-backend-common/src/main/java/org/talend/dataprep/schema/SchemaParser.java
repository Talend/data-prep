//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.InputStream;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Represents a class able to parse a data set content and return a list of {@link ColumnMetadata metadata} out of it.
 */
public interface SchemaParser {

    /**
     * <p>
     * Parses the provided content and extract {@link ColumnMetadata column} information. Implementations are encouraged
     * to returns as fast as possible from this method (possibly without processing the whole <code>content</code>
     * parameter).
     * </p>
     * 
     * @param request container with informations needed to parse the raw data
     * @return {@link SchemaParserResult} containing a list of {@link ColumnMetadata metadata}. When no column name/type
     * can be created, implementations are expected to generate names and select
     * {@link org.talend.dataprep.api.type.Type#STRING string} as type.
     */
    SchemaParserResult parse(Request request);

    /**
     * Schema parser request.
     */
    class Request {

        /** The dataset content as input stream. */
        InputStream content;

        /** The dataset metadata. */
        DataSetMetadata metadata;

        /**
         * Constructor.
         *
         * @param content The data set content. It should never be <code>null</code>.
         * @param metadata The data set metadata, to be used to retrieve parameters needed to understand format in
         * <code>content</code>.
         */
        public Request(InputStream content, DataSetMetadata metadata) {
            this.content = content;
            this.metadata = metadata;
        }

        /**
         * @return the dataset content.
         */
        public InputStream getContent() {
            return content;
        }

        /**
         * @return the dataset metadata.
         */
        public DataSetMetadata getMetadata() {
            return metadata;
        }
    }

}
