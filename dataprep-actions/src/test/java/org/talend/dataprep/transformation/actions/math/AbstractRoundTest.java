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

package org.talend.dataprep.transformation.actions.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Base class for all round tests.
 */
public abstract class AbstractRoundTest<T extends AbstractActionMetadata> extends AbstractMetadataBaseTest<T> {

    protected AbstractRoundTest(T action) {
        super(action);
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    protected void testCommon(String input, String expected) {
        testCommon(input, expected, null);
    }

    protected void testCommon(String input, String expected, Integer precision) {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", input);
        final DataSetRow row = new DataSetRow(values);

        if (precision != null) {
            getParameters().put(AbstractRound.PRECISION, "" + precision);
        }

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, getParameters()));

        // then
        assertEquals(expected, row.get("0000"));
    }


    @Test
    public void shouldListParameters() throws Exception {

        // given
        List<String> expectedParameters = getExpectedParametersName();

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertThat(parameters.size(), is(expectedParameters.size()));
        assertEquals(parameters.stream().map(Parameter::getName).filter(expectedParameters::contains).count(), expectedParameters.size());
    }

    protected abstract List<String> getExpectedParametersName();

    protected abstract Map<String, String> getParameters();
}
