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

import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Update the schema from an updated metadata.
 */
public interface SchemaUpdater {

    /**
     * Return true if this schema updated accepts this metadata.
     *
     * @param metadata the metadata to update.
     * @return true if this schema updater can update the given metadata.
     */
    boolean accept(DataSetMetadata metadata);

    /**
     * Update the schema of the given metadata.
     *
     * @param request the schema parser request.
     * @return the format guesser result.
     */
    FormatGuesser.Result updateSchema(SchemaParser.Request request);

    /**
     * @return the format guess.
     */
    FormatGuess getFormatGuess();

}
