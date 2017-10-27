// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.helper.api;

import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    public String action;

    // not to be loaded by jackson but to be inferred from steps attribute @see PreparationDetails
    public String id;

    public EnumMap<ActionParamEnum, String> parameters = new EnumMap<>(ActionParamEnum.class);

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Action))
            return false;

        Action action1 = (Action) o;

        if (action != null ? !action.equals(action1.action) : action1.action != null)
            return false;
        return parameters != null ? parameters.equals(action1.parameters) : action1.parameters == null;
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}