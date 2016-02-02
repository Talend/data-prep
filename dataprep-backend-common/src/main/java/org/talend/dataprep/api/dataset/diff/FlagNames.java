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

package org.talend.dataprep.api.dataset.diff;

/**
 * Interface that holds constants on flag names.
 */
public final class FlagNames {

    /** Key to put in the result values that tells that there are differences at column level. */
    public static final String DIFF_KEY = "__tdpDiff";

    /** Key to put in the result values that tells that there is a difference at row level. */
    public static final String ROW_DIFF_KEY = "__tdpRowDiff";

    /** Key to put in the result values that tells that there is a difference at column level. */
    public static final String COLUMN_DIFF_KEY = "__tdpColumnDiff";

    /**
     * Default private constructor.
     */
    private FlagNames() {
        // private constructor for utility class
    }

}
