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
 * @see Modulo
 */
public class Modulo extends AbstractMetadataBaseTest {

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
    public void pow_with_constant() {
        // given
        DataSetRow row = getRow("6", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "2");

        // when
        //ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("3.0", row.get("0003"));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_modulo").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }
}
