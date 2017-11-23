package org.talend.dataprep.transformation.actions.math;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

/**
 * Unit test for the Pow action.
 *
 * @see ModuloTest
 */
public class ModuloTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private Modulo action = new Modulo();

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = PowTest.class.getResourceAsStream("moduloAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("modulo", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.MATH.getDisplayName()));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
        assertEquals(6, parameters.size());
    }

    @Test
    public void should_calc_mod() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.MODE_PARAMETER, Modulo.CONSTANT_MODE);
        parameters.put(Modulo.DIVISOR, "5");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("1", row.get("0003"));
    }

    @Test
    public void should_calc_mod_with_decimal() {
        // given
        final DataSetRow row = builder() //
                .with(value("6.5").type(Type.STRING).name("0000")) //
                .with(value("5").type(Type.STRING).name("0001")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(Modulo.MODE_PARAMETER, Modulo.OTHER_COLUMN_MODE);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected =
                ColumnMetadata.Builder.column().id(3).name("0000 % 0001").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
        assertEquals("1.5", row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_empty_divisor() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.MODE_PARAMETER, Modulo.CONSTANT_MODE);
        parameters.put(Modulo.DIVISOR, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000 % ").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
        assertEquals(null, row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_char() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.MODE_PARAMETER, Modulo.CONSTANT_MODE);
        parameters.put(Modulo.DIVISOR, "a");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(null, row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_alpha_other_column() {
        // given
        final DataSetRow row = builder() //
                .with(value("6.5").type(Type.STRING).name("0000")) //
                .with(value("a").type(Type.STRING).name("0001")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(Modulo.MODE_PARAMETER, Modulo.OTHER_COLUMN_MODE);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected =
                ColumnMetadata.Builder.column().id(3).name("0000 % 0001").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
        assertEquals(null, row.get("0003"));
    }

    @Test
    public void should_not_calc_without_divisor() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.MODE_PARAMETER, Modulo.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(null, row.get("0003"));
    }

    @Test
    public void should_not_calc_without_column_parameter() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.MODE_PARAMETER, Modulo.OTHER_COLUMN_MODE);
        parameters.remove(Modulo.SELECTED_COLUMN_PARAMETER);
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(null, row.get("0003"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }
}
