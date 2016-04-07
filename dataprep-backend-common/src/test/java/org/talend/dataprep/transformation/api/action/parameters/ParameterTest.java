//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.parameters;

import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;

/**
 * Unit test for the parameter class. Mostly check the equals and json de/serialization
 */
public class ParameterTest extends ParameterBaseTest {

    @Test
    public void shouldSerializeToJsonWithoutEmptyConfiguration() throws IOException {
        // given
        Parameter expected = new Parameter("column_id", ParameterType.STRING, "0001", true, false);

        // when
        StringWriter out = new StringWriter();
        builder.build().writer().writeValue(out, expected);

        // then
        assertThat(out.toString(), sameJSONAs(IOUtils.toString(this.getClass().getResourceAsStream("textParameter.json"))));
    }

}