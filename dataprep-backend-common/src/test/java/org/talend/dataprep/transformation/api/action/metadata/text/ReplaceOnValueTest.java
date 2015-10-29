// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.text;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.*;
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.CELL_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.REPLACE_ENTIRE_CELL_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.REPLACE_VALUE_PARAMETER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Test class for Replace value action
 */
public class ReplaceOnValueTest {

    private ReplaceOnValue action = new ReplaceOnValue();

    @Test
    public void should_return_common_and_specific_parameters() {
        // when
        final List<Parameter> actionParams = action.getParameters();

        // then
        assertThat(actionParams, hasSize(7));

        final List<String> paramNames = actionParams.stream().map(Parameter::getName).collect(toList());
        assertThat(paramNames, IsIterableContainingInAnyOrder.containsInAnyOrder(COLUMN_ID.getKey(), //
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
        final boolean accepted = action.acceptColumn(column);

        // then
        assertThat(accepted, is(true));
    }

    @Test
    public void should_reject_non_string_typed_column() {
        // given
        final ColumnMetadata column = new ColumnMetadata();
        column.setType(BOOLEAN.toString());

        // when
        final boolean accepted = action.acceptColumn(column);

        // then
        assertThat(accepted, is(false));
    }

    @Test
    public void should_replace_the_value_that_match_on_the_specified_column_entire() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James Hetfield");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, "James");
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "true");

        // when
        action.applyOnColumn(row, null, parameters, columnId);

        // then
        assertThat(row.get(columnId), is("Jimmy"));
    }

    @Test
    public void testComputeNewValue() {
        // Case text when matches:
        Assert.assertEquals("Bob Dylan", action.computeNewValue("Robert Dylan", "Robert", "Bob", false));
        Assert.assertEquals("Bob", action.computeNewValue("Robert Dylan", "Robert", "Bob", true));
        Assert.assertEquals("I listen to Bob Dylan every day", action.computeNewValue("I listen to Robert Dylan every day", "Robert", "Bob", false));

        // Case text when don't match:
        Assert.assertEquals("Robert Dylan", action.computeNewValue("Robert Dylan", "Andy", "Bob", false));
        Assert.assertEquals("Robert Dylan", action.computeNewValue("Robert Dylan", "Andy", "Bob", true));

        // Case regexp when matches:
        Assert.assertEquals("Bob", action.computeNewValue("Robert Dylan", "Robert.*", "Bob", false));
        Assert.assertEquals("Bob", action.computeNewValue("Robert Dylan", "Robert.*", "Bob", true));
        Assert.assertEquals("I want to break free", action.computeNewValue("I want 2 break free", "\\d", "to", false));
        Assert.assertEquals("to", action.computeNewValue("I want 2 break free", ".*\\d.*", "to", false));
        Assert.assertEquals("to", action.computeNewValue("I want 2 break free", "\\d", "to", true));

        // Case regexp when don't match:
        Assert.assertEquals("Robert Dylan", action.computeNewValue("Robert Dylan", ".*Andy.*", "Bob", false));
        Assert.assertEquals("Robert Dylan", action.computeNewValue("Robert Dylan", "Andy.*", "Bob", true));

        Assert.assertEquals("XXX_EN_YYY", action.computeNewValue("XXX_FR_YYY", "FR", "EN", false));
        Assert.assertEquals("XXX_FR_YYY", action.computeNewValue("XXX_FR_YYY", "FOO", "EN", false));
        Assert.assertEquals("EN", action.computeNewValue("XXX_FR_YYY", "FR", "EN", true));

        Assert.assertEquals("XXX_EN_YYY", action.computeNewValue("XXX_FR_YYY", "F.", "EN", false));
        Assert.assertEquals("XXX_FR_YYY", action.computeNewValue("XXX_FR_YYY", "G.", "EN", false));
        Assert.assertEquals("EN", action.computeNewValue("XXX_FR_YYY", "F.", "EN", true));

        Assert.assertEquals("XXX_EN_YYY", action.computeNewValue("XXX_David Bowie_YYY", "_.*_", "_EN_", false));
        Assert.assertEquals("XXX_David 2 Bowie_YYY",
                action.computeNewValue("XXX_David 2 Bowie_YYY", "_[a-zA-Z]*_", "_EN_", false));
        Assert.assertEquals("XXX_EN_YYY", action.computeNewValue("XXX_David 2 Bowie_YYY", "_[a-zA-Z0-9 ]*_", "_EN_", false));

        Assert.assertEquals("XXX_YYY", action.computeNewValue("XXX_FR_YYY", "FR_", "", false));

    }

    @Test
    public void should_replace_the_value_that_match_on_the_specified_column() {
        Assert.assertEquals("Jimmy Hetfield", action.computeNewValue("James Hetfield", "James", "Jimmy", false));
        Assert.assertEquals("Jimmy", action.computeNewValue("James Hetfield", "James", "Jimmy", true));
    }

    @Test
    public void should_NOT_replace_the_value_that_DOESNT_match_on_the_specified_column() {
        Assert.assertEquals("Toto", action.computeNewValue("Toto", "James", "Jimmy", false));
        Assert.assertEquals("Toto", action.computeNewValue("Toto", "James", "Jimmy", true));
    }

    @Test
    public void should_NOT_replace_the_value_because_the_column_DOES_NOT_exist() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "Toto");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, "James");
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");

        // when
        action.applyOnColumn(row, null, parameters, "no column here");

        // then
        assertThat(row.get(columnId), is("Toto"));
    }

    @Test
    public void should_replace_the_value_that_match_on_the_specified_cell() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, "James");
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");

        // when
        action.applyOnCell(row, null, parameters, 85L, columnId);

        // then
        assertThat(row.get(columnId), is("Jimmy"));
    }

    @Test
    public void test_TDP_796() {
        String from = "bridge.html?region=FR";
        String regexp = "bridge.html?region=FR";
        String to = "pont.html?region=FR";
        Assert.assertEquals(to, action.computeNewValue(from, regexp, to, false));
    }

    @Test
    public void should_replace_many_values_that_match_on_the_specified_cell() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "James Cleveland James");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, "James");
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");

        // when
        action.applyOnCell(row, null, parameters, 85L, columnId);

        // then
        assertThat(row.get(columnId), is("Jimmy Cleveland Jimmy"));
    }

    @Test
    public void should_NOT_replace_the_value_that_DOESNT_match_on_the_specified_cell() {
        // given
        final String columnId = "firstname";

        final Map<String, String> values = new HashMap<>();
        values.put(columnId, "Toto");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CELL_VALUE_PARAMETER, "James");
        parameters.put(REPLACE_VALUE_PARAMETER, "Jimmy");
        parameters.put(REPLACE_ENTIRE_CELL_PARAMETER, "false");

        // when
        action.applyOnCell(row, null, parameters, 85L, columnId);

        // then
        assertThat(row.get(columnId), is("Toto"));
    }

    @Test
    public void should_replace_the_value_because_regexp() {
        Assert.assertEquals("replaced", action.computeNewValue("password swordfish with Halle Berry", ".*Halle.*", "replaced", false));
        Assert.assertEquals("replaced", action.computeNewValue("password swordfish with Halle Berry", ".*Halle.*", "replaced", true));
    }

    @Test
    public void test_TDP_663() {
        Assert.assertEquals("password swordfish with Halle Berry", action.computeNewValue("password swordfish with Halle Berry", "*", "replaced", true));
        Assert.assertEquals("password swordfish with Halle Berry", action.computeNewValue("password swordfish with Halle Berry", "*", "replaced", false));
    }

}
