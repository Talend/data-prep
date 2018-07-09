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

package org.talend.dataprep.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.transformation.actions.text.LowerCase;
import org.talend.dataprep.transformation.actions.text.UpperCase;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

@RunWith(MockitoJUnitRunner.class)
public class InjectorUtilTest {

    @Mock
    private ActionRegistry actionRegistry;

    @InjectMocks
    private InjectorUtil injectorUtil;

    @Test
    public void testInjectPreparationDetailsDTO() throws IllegalAccessException, InstantiationException {
        PreparationDetailsDTO prep = new PreparationDetailsDTO();
        List<Action> actions = new ArrayList<>();

        actions.add(getSimpleAction("uppercase", "column_name", "lastname"));
        actions.add(getSimpleAction("lowercase", "column_name", "lastname"));

        when(actionRegistry.get("uppercase")).thenReturn(UpperCase.class.newInstance());
        when(actionRegistry.get("lowercase")).thenReturn(LowerCase.class.newInstance());

        PreparationDetailsDTO detailsPrep = injectorUtil.injectPreparationDetails(actions, prep);

        detailsPrep.getMetadata().forEach(af -> {
            af.getParameters().forEach(p -> {

                // we check if action create new column then it is on readonly mode
                if (p.getName().equals(CREATE_NEW_COLUMN)) {
                    assertTrue(p.isReadonly());
                }
            });
        });

        assertEquals("Number of action should be the same", actions.size(), detailsPrep.getActions().size());
        assertEquals("Number of ActionForm should be the same", actions.size(), detailsPrep.getMetadata().size());

    }

    private static Action getSimpleAction(final String actionName, final String paramKey, final String paramValue) {
        final Action action = new Action();
        action.setName(actionName);
        action.getParameters().put(paramKey, paramValue);

        return action;
    }
}
