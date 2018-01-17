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

package org.talend.dataprep.transformation.api.action.validation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.talend.dataprep.BaseErrorCodes.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public class ActionMetadataValidationTest extends ServiceBaseTest {

    @Autowired
    private ActionMetadataValidation validator;

    @Test
    public void checkScopeConsistency_should_pass() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "0001");
        ActionDefinition actionMock = new ActionMetadataExtendingColumn();

        // when
        validator.checkScopeConsistency(actionMock, parameters);

        // then : should not throw exception
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_missing_scope() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("column_id", "0001");
        ActionDefinition actionMock = new ActionMetadataExtendingColumn();

        // when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because param scope is missing");
        }

        // then
        catch (final TalendRuntimeException e) {
            assertThat(e.getCode(), is(MISSING_ACTION_SCOPE));
        }
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_unsupported_scope() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "0001");
        ActionMetadataExtendingColumn actionMock = new ActionMetadataExtendingColumn();

        // when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because line scope is not supported by cut (for example)");
        }

        // then
        catch (final TalendRuntimeException e) {
            assertThat(e.getCode(), is(UNSUPPORTED_ACTION_SCOPE));
        }
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_missing_scope_mandatory_parameter() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("row_id", "0001");
        ActionMetadataExtendingColumn actionMock = new ActionMetadataExtendingColumn();

        // when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because cell scope requires column_id (for example)");
        }

        // then
        catch (final TalendRuntimeException e) {
            assertThat(e.getCode(), is(MISSING_ACTION_SCOPE_PARAMETER));
        }
    }

    private static class ActionMetadataExtendingColumn extends AbstractActionMetadata implements ColumnAction {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getCategory(Locale locale) {
            return null;
        }

        @Override
        public boolean acceptField(ColumnMetadata column) {
            return false;
        }

        @Override
        public Set<Behavior> getBehavior() {
            return null;
        }

        @Override
        public void applyOnColumn(DataSetRow row, ActionContext context) {

        }
    }
}
