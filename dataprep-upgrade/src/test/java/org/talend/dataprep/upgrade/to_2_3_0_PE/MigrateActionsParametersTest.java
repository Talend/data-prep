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

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

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

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 1;
    }
}
