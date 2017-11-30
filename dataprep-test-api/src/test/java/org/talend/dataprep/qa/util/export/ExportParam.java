package org.talend.dataprep.qa.util.export;

import org.talend.dataprep.qa.util.StepParamType;

public interface ExportParam {

    StepParamType getType();

    String getName();

    String getJsonName();

}
