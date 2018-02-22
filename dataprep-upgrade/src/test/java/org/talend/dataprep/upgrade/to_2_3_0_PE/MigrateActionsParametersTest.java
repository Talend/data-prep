/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.upgrade.to_2_3_0_PE;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

public class MigrateActionsParametersTest extends Base_2_3_0_PE_Test {

    @Autowired
    private MigrateActionsParameters task;

    @Autowired
    private PreparationRepository preparationRepository;

    @Test
    public void shouldAddCreateNewColumnParameter() {
        // given a datastore built with one preparation with 3 steps
        // when
        task.run();

        // then
        Optional<Action> numericOpsAction = preparationRepository //
                .list(PreparationActions.class) //
                .filter(pa -> pa.getId().equals("cde7e7f945afe3e7e39cb26b00705c7464ba6d3a"))
                .flatMap(a -> a.getActions().stream()) //
                .filter(a -> "numeric_ops".equals(a.getName())).findFirst();

        assertTrue(numericOpsAction.isPresent());
        assertEquals(TRUE.toString(), numericOpsAction.get().getParameters().get(CREATE_NEW_COLUMN));

    }

    @Test public void shouldUpdateFormatPhoneNumberAction() {
        // given a datastore built with one preparation with 3 steps
        // when
        task.run();

        // then
        List<Action> actions = preparationRepository //
                .list(PreparationActions.class) //
                .filter(pa -> pa.getId().equals("6cae9216b36b9cdbc05807109d84f420020a145e"))
                .flatMap(a -> a.getActions().stream()) //
                .filter(a -> FormatPhoneNumber.ACTION_NAME.equals(a.getName()))
                .sorted(Comparator.comparing(o -> o.getParameters().get("column_id")))
                .collect(Collectors.toList());

        for (Action action : actions) {
            assertEquals("other_region", action.getParameters().get("region_code"));
        }

        Action action1 = actions.get(0);
        assertEquals("international", action1.getParameters().get("format_type"));
        assertEquals("0012", action1.getParameters().get("column_id"));

        Action action2 = actions.get(1);
        assertEquals("national", action2.getParameters().get("format_type"));
        assertEquals("0013", action1.getParameters().get("column_id"));
    }

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 0;
    }
}
