//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.transformation.actions.text;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.*;
import static org.talend.dataprep.transformation.actions.text.ReplaceOnValue.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Test class for Replace value action
 */
public class ReplaceOnValueTest extends AbstractMetadataBaseTest<ReplaceOnValue> {

    public ReplaceOnValueTest() {
        super(new ReplaceOnValue());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    private ActionContext buildPatternActionContext(String regex, String replacement, boolean replace) {
        ActionContext context = new ActionContext(new TransformationContext());
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ReplaceOnValue.CELL_VALUE_PARAMETER, regex);
        parameters.put(ReplaceOnValue.REPLACE_VALUE_PARAMETER, replacement);
        parameters.put(ReplaceOnValue.REPLACE_ENTIRE_CELL_PARAMETER, String.valueOf(replace));
        context.setParameters(parameters);
        action.compile(context);
        return context;
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("replace_on_value", action.getName());
    }

    @Test
    public void test_category() throws Exception {
        assertEquals("strings", action.getCategory(Locale.US));
    }

    @Test
    public void should_return_common_and_specific_parameters() {
        // when
        final List<Parameter> actionParams = action.getParameters(Locale.US);

        // then
        assertThat(actionParams, hasSize(8));

        final List<String> paramNames = actionParams.stream().map(Parameter::getName).collect(toList());
        assertThat(paramNames, IsIterableContainingInAnyOrder.containsInAnyOrder(COLUMN_ID.getKey(), //
                ActionsUtils.CREATE_NEW_COLUMN, //
                ROW_ID.getKey(), //
                SCOPE.getKey(), //
                FILTER.getKey(), //
                CELL_VALUE_PARAMETER, //
                REPLACE_VALUE_PARAMETER, //
                REPLACE_ENTIRE_CELL_PARAMETER));
    }

    @Test
    public void should_accept_string_typed_column() {
        // given
        final ColumnMetadata column = new ColumnMetadata();
        column.setType(STRING.toString());

        // when
        final boolean accepted = action.acceptField(column);

        // then
        assertThat(accepted, is(true));
    }

    @Test
    public void should_reject_non_string_typed_column() {
        // given
        final ColumnMetadata column = new ColumnMetadata();
        column.setType(BOOLEAN.toString());

        // when
        final boolean accepted = action.acceptField(column);

        // then
        assertThat(accepted, is(false));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final String columnId = "0000";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James Hetfield");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, generateJson("James", ReplaceOnValueHelper.STARTS_WITH_MODE));
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "true");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId);

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get(columnId), is("James Hetfield"));
        assertThat(row.get("0001"), is("Jimmy"));
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(1).name("0000_replace").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        // given
        final String columnId = "0000";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James Hetfield");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, generateJson("James", ReplaceOnValueHelper.STARTS_WITH_MODE));
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "true");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get(columnId), is("Jimmy"));
    }

    @Test
    public void test_TDP_1204() {
        // given
        final String columnId = "0000";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, generateJson("", ReplaceOnValueHelper.EQUALS_MODE));
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "true");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get(columnId), is("Jimmy"));
    }

    /**
     * Test with an invalid regex pattern as token and mode is not REGEX.
     */
    @Test
    public void should_replace_the_value_that_match_on_the_specified_column_entireh() {
        // given
        final String columnId = "0000";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James (etfield");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, generateJson("(", ReplaceOnValueHelper.CONTAINS_MODE));
        parameters.put(REPLACE_VALUE_PARAMETER, "H");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get(columnId), is("James Hetfield"));
    }

    /**
     * This test case covers all cases describe in TDP-951 description.
     */
    @Test
    public void test_TDP_951() {
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.EQUALS_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.EQUALS_MODE), "EN", true), "XXX_FR_YYY"));

        assertEquals("Barfoo", action.computeNewValue(
                buildPatternActionContext(generateJson("Foobar", ReplaceOnValueHelper.EQUALS_MODE), "Barfoo", false), "Foobar"));
        assertEquals("Barfoo", action.computeNewValue(
                buildPatternActionContext(generateJson("Foobar", ReplaceOnValueHelper.EQUALS_MODE), "Barfoo", true), "Foobar"));


        assertEquals("XXX_EN_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.CONTAINS_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("EN", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.CONTAINS_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("XXX_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR_", ReplaceOnValueHelper.CONTAINS_MODE), "", false), "XXX_FR_YYY"));

        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("EN_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", false), "FR_YYY"));
        assertEquals("EN", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", true), "FR_YYY"));

        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("XXX_EN", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", false), "XXX_FR"));
        assertEquals("EN", action.computeNewValue(
                buildPatternActionContext(generateJson("FR", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", true), "XXX_FR"));

        // Nothing to do because doesn't match:
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.EQUALS_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.EQUALS_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.CONTAINS_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.CONTAINS_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                        buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", false),
                        "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.STARTS_WITH_MODE), "EN", true), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", false), "XXX_FR_YYY"));
        assertEquals("XXX_FR_YYY", action.computeNewValue(
                buildPatternActionContext(generateJson("FOO", ReplaceOnValueHelper.ENDS_WITH_MODE), "EN", true), "XXX_FR_YYY"));

        assertEquals("XXX_foobar_YYY", action.computeNewValue(
                        buildPatternActionContext(generateJson("t.t.", ReplaceOnValueHelper.REGEX_MODE), "foobar", false),
                        "XXX_toto_YYY"));
        assertEquals("XXX_foobar_YYY", action.computeNewValue(
                        buildPatternActionContext(generateJson("t.t.", ReplaceOnValueHelper.REGEX_MODE), "foobar", false),
                        "XXX_titi_YYY"));
        assertEquals("XXX_ZZ_YYY",
                action.computeNewValue(
                        buildPatternActionContext(generateJson("_[a-z ]*_", ReplaceOnValueHelper.REGEX_MODE), "_ZZ_", false),
                        "XXX_this is a string_YYY"));
        assertEquals("foobar", action.computeNewValue(
                        buildPatternActionContext(generateJson("t.t.", ReplaceOnValueHelper.REGEX_MODE), "foobar", true),
                        "XXX_toto_YYY"));

        assertEquals("XXX-123-YYY", action.computeNewValue(
                        buildPatternActionContext(generateJson("_(\\d{3,})_", ReplaceOnValueHelper.REGEX_MODE), "-$1-", false),
                        "XXX_123_YYY"));
        assertEquals("123", action.computeNewValue(
                        buildPatternActionContext(generateJson("_(\\d{3,})_", ReplaceOnValueHelper.REGEX_MODE), "$1", true),
                        "XXX_123_YYY"));
    }

    @Test
    public void test_TDP_1227(){
        assertEquals("India", action.computeNewValue(
                buildPatternActionContext(generateJson("I(.*)", ReplaceOnValueHelper.REGEX_MODE), "J$2", true),
                "India"));
        assertEquals("India", action.computeNewValue(
                buildPatternActionContext(generateJson("I(.*)", ReplaceOnValueHelper.REGEX_MODE), "J$2", false),
                "India"));
    }

    @Test
    public void should_NOT_replace_the_value_because_the_column_DOES_NOT_exist() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "Toto");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, generateJson("Toto", ReplaceOnValueHelper.EQUALS_MODE));
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "no column here");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get(columnId), is("Toto"));
    }

    @Test
    public void should_replace_value_based_on_regex() {
        //given
        final String from = "bridge.html?region=FR";
        final String regexp = "bridge.html[?]region=FR";
        final String to = "pont.html?region=FR";

        //when
        final String result = action.computeNewValue(
                buildPatternActionContext(generateJson(regexp, ReplaceOnValueHelper.REGEX_MODE), to, false), from);

        //then
        assertThat(result, is(to));
    }

    @Test
    public void test_TDP_1502_match() {
        // given
        final String from = "Reference\nGI";
        final String regexp = "GI";
        final String to = "Joe";
        final String expected = "Reference\nJoe";

        // when
        final String result = action.computeNewValue(
                buildPatternActionContext(generateJson(regexp, ReplaceOnValueHelper.REGEX_MODE), to, false), from);

        // then
        assertThat(result, is(expected));
    }

    @Test
    public void test_TDP_1502_replace() {
        // given
        final String from = "Reference\nGI";
        final String regexp = "\n";
        final String to = " ";
        final String expected = "Reference GI";

        // when
        final String result = action.computeNewValue(
                buildPatternActionContext(generateJson(regexp, ReplaceOnValueHelper.REGEX_MODE), to, false), from);

        // then
        assertThat(result, is(expected));
    }

    @Test
    public void test_TDP_1502_replace_full() {
        // given
        final String from = "Reference\nGI";
        final String regexp = "\nGI";
        final String to = " Joe";
        final String expected = "Reference Joe";

        // when
        final String result = action.computeNewValue(
                buildPatternActionContext(generateJson(regexp, ReplaceOnValueHelper.REGEX_MODE), to, false), from);

        // then
        assertThat(result, is(expected));
    }

    @Test
    public void test_TDP_663() {
        for (String op : new String[] { ReplaceOnValueHelper.REGEX_MODE, ReplaceOnValueHelper.EQUALS_MODE,
                ReplaceOnValueHelper.CONTAINS_MODE, ReplaceOnValueHelper.STARTS_WITH_MODE,
                ReplaceOnValueHelper.ENDS_WITH_MODE }) {
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("*", op), "replaced", false), "password swordfish with Halle Berry"));
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("*", op), "replaced", true), "password swordfish with Halle Berry"));
        }
    }

    @Test
    public void test_TDP_958_emptyPattern() {
        for (String op : new String[] { ReplaceOnValueHelper.REGEX_MODE, ReplaceOnValueHelper.EQUALS_MODE,
                ReplaceOnValueHelper.CONTAINS_MODE, ReplaceOnValueHelper.STARTS_WITH_MODE,
                ReplaceOnValueHelper.ENDS_WITH_MODE }) {
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("", op), "replaced", false), "password swordfish with Halle Berry"));
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("", op), "replaced", true), "password swordfish with Halle Berry"));
        }
    }

    @Test
    public void test_TDP_958_invalidPattern() {
        for (String op : new String[] { ReplaceOnValueHelper.REGEX_MODE, ReplaceOnValueHelper.EQUALS_MODE,
                ReplaceOnValueHelper.CONTAINS_MODE, ReplaceOnValueHelper.STARTS_WITH_MODE,
                ReplaceOnValueHelper.ENDS_WITH_MODE }) {
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("^(", op), "replaced", false), "password swordfish with Halle Berry"));
            assertEquals("password swordfish with Halle Berry", action.computeNewValue(
                    buildPatternActionContext(generateJson("^(", op), "replaced", true), "password swordfish with Halle Berry"));
        }
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
