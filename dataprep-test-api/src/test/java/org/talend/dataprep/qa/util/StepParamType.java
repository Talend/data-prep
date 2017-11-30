package org.talend.dataprep.qa.util;

/**
 * List the kind os parameter that can be used in a Cucumber Step.
 */
public enum StepParamType {
    /** Represent a parameter that won't be send directly to the API. */
    IN, //
    /** Represent a parameter that will be send to the API but that isn't given directly in the step datatable. */
    OUT, //
    /** Represent a parameter given in the step datatablen that will be send directly to the API. */
    IN_OUT;
}
