package org.talend.dataprep.qa.util.export;

import org.talend.dataprep.qa.util.StepParamType;

/**
 * All enum representing specific export parameters should implements this interface.
 */
public interface ExportParam {

    /** The parameter {@link org.talend.dataprep.qa.util.StepParamType}. */
    StepParamType getType();

    /** The parameter's name. */
    String getName();

    /** The parameter's json api name.*/
    String getJsonName();

}
