// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.i18n.custom.actions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;

/**
 * Fake action class to test action message
 */
public class TestAction extends AbstractActionMetadata {
    @Override
    public String getName() {
        return "TestAction";
    }

    @Override
    public String getCategory() {
        return "No category";
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<Parameter> getParameters() {
        return ActionsBundle.attachToAction(Collections.singletonList(new Parameter("customParameter", ParameterType.STRING, "", false, false, "")), this);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.singleton(Behavior.VALUES_ALL);
    }
}
