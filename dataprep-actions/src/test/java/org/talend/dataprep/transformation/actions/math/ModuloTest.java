package org.talend.dataprep.transformation.actions.math;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
    public void should_calc_mod() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.DIVISOR, "5");
        parameters.put(Modulo.PRECISION, "1");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1.0", row.get("0000"));
    }

    @Test
    public void should_calc_mod_with_decimal() {
        // given
        DataSetRow row = getRow("6.5", "3", "Done !");

        parameters.put(Modulo.DIVISOR, "5");
        parameters.put(Modulo.PRECISION, "0");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("2", row.get("0000"));
    }

    @Test
    public void should_calc_mod_with_empty_precision() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.DIVISOR, "5");
        parameters.put(Modulo.PRECISION, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1", row.get("0000"));
    }

    @Test
    public void should_not_calc_mod_with_empty_divisor() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(Modulo.DIVISOR, "");
        parameters.put(Modulo.PRECISION, "0");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("6", row.get("0000"));
    }

    @Test
    public void test_getPrecision() {
        parameters.put(Modulo.PRECISION, "");
        assertEquals(0, action.getPrecision(parameters));

        parameters.put(Modulo.PRECISION, "3");
        assertEquals(3, action.getPrecision(parameters));

        parameters.put(Modulo.PRECISION, "-1");
        assertEquals(0, action.getPrecision(parameters));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_modulo").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }
}
