package org.talend.dataprep.parameters;

import java.util.List;

public abstract class Parameterizable {

    /** Does this format type need more parameters? (ui will open a new form in this case). */
    private final boolean needParameters;

    public Parameterizable(final boolean needParameters) {
        this.needParameters = needParameters;
    }

    /**
     * @return true if parameters are needed.
     */
    public boolean isNeedParameters() {
        return needParameters;
    }

    /**
     * @return the list of needed parameters.
     */
    public abstract List<Parameter> getParameters();

}
