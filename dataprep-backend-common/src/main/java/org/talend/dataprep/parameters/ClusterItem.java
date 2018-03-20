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

package org.talend.dataprep.parameters;

import java.util.ArrayList;
import java.util.List;

public class ClusterItem {

    private final List<Parameter> parameters = new ArrayList<>();

    private final Parameter replace;

    private ClusterItem(final List<Parameter> parameters, final Parameter replace) {
        this.parameters.addAll(parameters);
        this.replace = replace;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Parameter getReplace() {
        return replace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Parameter> parameters = new ArrayList<>();

        private Parameter replace;

        public Builder parameter(final Parameter parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder replace(final Parameter replace) {
            this.replace = replace;
            return this;
        }

        public ClusterItem build() {
            return new ClusterItem(parameters, replace);
        }
    }
}
