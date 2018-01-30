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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.junit.Assert.*;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;
import static org.talend.dataprep.upgrade.to_2_1_0_PE.SplitReplaceOnValueAction.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Unit tests for the SplitReplaceOnValueAction.
 */
public class SplitReplaceOnValueActionTest extends Base_2_1_0_PE_Test {

    /** The task to test. */
    @Autowired
    private SplitReplaceOnValueAction task;

    @Autowired
    private PreparationRepository repository;

    /**
     * @return the task id.
     */
    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    /**
     * @return the expected task order.
     */
    @Override
    protected int getExpectedTaskOrder() {
        return 4;
    }

    @Test
    public void shouldSplitActions() {

        // when
        task.run();

        // then
        final List<PreparationActions> prepActionsList = repository.list(PreparationActions.class).collect(Collectors.toList());

        assertEquals(12, prepActionsList.size());

        int actionUpdated = 0;
        int actionNotUpdated = 0;
        for (PreparationActions prepActions : prepActionsList) {
            for (Action action : prepActions.getActions()) {
                if (REPLACE_CELL_VALUE.equals(action.getName())) {
                    final Map<String, String> parameters = action.getParameters();
                    assertTrue(parameters.containsKey("new_value"));
                    assertTrue(parameters.containsKey("original_value"));
                    assertFalse(parameters.containsKey(CELL_VALUE));
                    assertFalse(parameters.containsKey(REPLACE_VALUE));
                    actionUpdated++;
                } else {
                    actionNotUpdated++;
                }
            }
        }

        assertEquals(1, actionUpdated);
        assertEquals(33, actionNotUpdated);

    }

    @Test
    public void targetShouldBeVersion() throws Exception {
        assertEquals(VERSION, task.getTarget());
    }

}