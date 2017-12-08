// ============================================================================
//
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

package org.talend.dataprep.transformation.actions.common;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.util.*;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.parameters.Parameter;

public class AbstractActionMetadataTest {

    public static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    private static final AbstractActionMetadata doNothingAction = new AbstractActionMetadata() {

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
    };

    @Test
    public void testCreateNewColumn_defaultValue() throws Exception {
        // given
        AbstractActionMetadata action = doNothingAction;
        Map<String, String> emptyMap = new HashMap<>();

        // then
        // test that action will not create a new column:
        assertThat(ActionsUtils.doesCreateNewColumn(emptyMap, CREATE_NEW_COLUMN_DEFAULT), is(false));

        // test that 'create_new_column' parameter is present and set to false by default:
        final List<Parameter> parameters = action.getParameters(Locale.ENGLISH);
        assertEquals(4, parameters.size());
        final Parameter createNewColumnParam = parameters.get(4);
        assertEquals("Create new column", createNewColumnParam.getLabel());
        assertFalse(Boolean.parseBoolean( createNewColumnParam.getDefault()));
    }

    @Test
    public void testCreateNewColumn_wrongValue() throws Exception {
        // given
        AbstractActionMetadata action = doNothingAction;
        Map<String, String> emptyMap = new HashMap<>();
        emptyMap.put(CREATE_NEW_COLUMN, "tagada");

        // then
        // test that action will not create a new column:
        assertThat(ActionsUtils.doesCreateNewColumn(emptyMap, CREATE_NEW_COLUMN_DEFAULT), is(false));

        // test that 'create_new_column' parameter is present and set to false by default:
        final List<Parameter> parameters = action.getParameters(Locale.ENGLISH);
        assertEquals(4, parameters.size());
        final Parameter createNewColumnParam = parameters.get(4);
        assertEquals("Create new column", createNewColumnParam.getLabel());
        assertFalse(Boolean.parseBoolean( createNewColumnParam.getDefault()));
    }

    @Test
    public void testCreateNewColumn_defaultTrue() throws Exception {
        // given an action that by default creates new column (like 'compare numbers'):
        AbstractActionMetadata action = new AbstractActionMetadata() {

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
        };
        Map<String, String> emptyMap = new HashMap<>();

        // then
        // test that action will create a new column:
        // test that 'create_new_column' parameter is present and set to false by default:
        final List<Parameter> parameters = action.getParameters(Locale.ENGLISH);
        assertEquals(5, parameters.size());
        final Parameter createNewColumnParam = parameters.get(4);
        assertEquals("Create new column", createNewColumnParam.getLabel());
        assertTrue(Boolean.parseBoolean( createNewColumnParam.getDefault()));
    }

    @Test
    public void testCreateNewColumn_optionHiddenAndFalse() throws Exception {
        // given an action that always create new columns (like 'split'):
        AbstractActionMetadata action = new AbstractActionMetadata() {

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
        };
        Map<String, String> emptyMap = new HashMap<>();

        // then
        // test that action will not create a new column:
        assertThat(ActionsUtils.doesCreateNewColumn(emptyMap, CREATE_NEW_COLUMN_DEFAULT), is(false));

        // test that 'create_new_column' parameter is not present:
        final List<Parameter> parameters = action.getParameters(Locale.ENGLISH);
        assertEquals(4, parameters.size());

        for (Parameter param:parameters){
            assertNotEquals("Create new column", param.getLabel());
        }
    }

    @Test
    public void testCreateNewColumn_optionHiddenAndTrue() throws Exception {
        // given an action that never create new columns (like 'mask data'):
        AbstractActionMetadata action = new AbstractActionMetadata() {

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
        };
        Map<String, String> emptyMap = new HashMap<>();

        // then
        // test that action will not create a new column:
        assertThat(ActionsUtils.doesCreateNewColumn(emptyMap, CREATE_NEW_COLUMN_DEFAULT), is(false));

        // test that 'create_new_column' parameter is not present:
        final List<Parameter> parameters = action.getParameters(Locale.ENGLISH);
        assertEquals(4, parameters.size());

        for (Parameter param:parameters){
            assertNotEquals("Create new column", param.getLabel());
        }
    }

}
