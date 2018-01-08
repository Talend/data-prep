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
package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for FuzzyMatching action. Creates one consumer, and test it.
 *
 * @see FuzzyMatching
 */
public class FuzzyMatchingTest extends AbstractMetadataBaseTest<FuzzyMatching> {

    private Map<String, String> parameters;

    public FuzzyMatchingTest() {
        super(new FuzzyMatching());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(FuzzyMatchingTest.class.getResourceAsStream("levenshteinDistance.json"));
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("fuzzy_matching", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS.getDisplayName(Locale.US)));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.VALUE_PARAMETER, "pale zle");
        parameters.put(FuzzyMatching.SENSITIVITY, "5");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "true"));
    }

    @Test
    public void should_be_true_as_equals_distance_with_constant() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.VALUE_PARAMETER, "zale zne");
        parameters.put(FuzzyMatching.SENSITIVITY, "3");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "true"));
    }

    @Test
    public void should_be_false_as_not_less_equals_than_distance_with_constant() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.VALUE_PARAMETER, "zale zSQ");
        parameters.put(FuzzyMatching.SENSITIVITY, "3");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "false"));
    }

    @Test
    public void should_be_true_as_less_than_distance_with_column() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        values.put("0001", "pale zle");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.SENSITIVITY, "5");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "pale zle"), //
                        MapEntry.entry("0002", "true"));
    }

    @Test
    public void should_be_true_as_equals_distance_with_column() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        values.put("0001", "zale zne");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.SENSITIVITY, "3");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "zale zne"), //
                        MapEntry.entry("0002", "true"));
    }

    @Test
    public void should_be_false_as_not_less_equals_than_distance_with_column() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        values.put("0001", "zale zSQ");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(FuzzyMatching.SENSITIVITY, "3");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "zale zSQ"), //
                        MapEntry.entry("0002", "false"));
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(6, parameters.size());
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), "sensitivity")));
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), "mode")));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
        assertFalse(action.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
