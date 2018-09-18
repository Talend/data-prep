// ============================================================================
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

package org.talend.dataprep.parameters;

/**
 * List of available parameter type.
 */
public enum ParameterType {
    /**
     * A String parameter
     */
    STRING,
    /**
     * A Integer (numeric) parameter
     */
    INTEGER,
    /**
     * A parameter from a list of choices.
     */
    SELECT,
    /**
     * A column id in a data set / preparation
     */
    COLUMN,
    /**
     * A date parameter (expected to show a date picker in UI).
     */
    DATE,
    /**
     * A date time parameter (expected always contain an UTC datetime).
     */
    UTC_DATETIME,
    /**
     * A boolean parameter (true/false) parameter.
     */
    BOOLEAN,
    /**
     * A filter parameter used to filter values in current view.
     */
    FILTER,
    /**
     * A regular expression parameter
     */
    REGEX,
    /**
     * This kind of parameter allow users to pass along multiple values as an array.
     */
    LIST,
    /**
     * This kind of parameter allow users to pass a user local file.
     */
    FILE,
    /**
     * This kind of parameter allow users to create a "hidden" field (for a password).
     */
    PASSWORD;

    public String asString() {
        return this.name().toLowerCase();
    }
}
