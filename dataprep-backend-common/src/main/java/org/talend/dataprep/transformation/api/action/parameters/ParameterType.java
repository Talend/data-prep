package org.talend.dataprep.transformation.api.action.parameters;

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
    LIST;
    public String asString() {
        return this.name().toLowerCase();
    }
}
