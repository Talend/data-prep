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

package org.talend.dataprep.actions;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Function;

import org.apache.avro.generic.IndexedRecord;

/**
 * An action parser parses the JSON array that contains the actions to apply to the {@link IndexedRecord records}.
 */
public interface ActionParser {

    /**
     * Returns a function able to transform an {@link IndexedRecord record} using preparation specified in <code>preparation</code>.
     *
     * @param preparationId A JSON of a Data Prep preparation to be applied to {@link IndexedRecord records}.
     * @return A {@link Function} able to apply all <code>preparation</code> to the {@link IndexedRecord record}.
     */
    Function<IndexedRecord, IndexedRecord> parse(String preparationId);

}
