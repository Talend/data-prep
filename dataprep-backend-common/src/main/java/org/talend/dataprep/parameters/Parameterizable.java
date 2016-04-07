package org.talend.dataprep.parameters;

import java.util.List;

public class Parameterizable {

    /** Does this format type need more parameters? (ui will open a new form in this case). */
    protected final boolean needParameters;

    /** List of extra parameters needed for this format (i.e separator for csv files etc...). */
    protected final List<Parameter> parameters;

    public Parameterizable(final List<Parameter> parameters, final boolean needParameters) {
        this.parameters = parameters;
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
    public List<Parameter> getParameters() {
        return parameters;
    }
}
