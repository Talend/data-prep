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

package org.talend.dataprep.api.dataset.row;

/**
 * Flag used for the diff.
 */
public enum Flag {

    DELETE("delete"),
    NEW("new"),
    UPDATE("update");

    /** Expected frontend value. */
    private final String value;

    /**
     * Constructor with the given value.
     *
     * @param value the expected frontend value.
     */
    Flag(String value) {
        this.value = value;
    }

    /**
     * @return the frontend expected value.
     */
    public String getValue() {
        return value;
    }

}
