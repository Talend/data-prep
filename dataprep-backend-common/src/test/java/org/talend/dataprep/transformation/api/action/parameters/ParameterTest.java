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

package org.talend.dataprep.transformation.api.action.parameters;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

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
        Parameter expected = Parameter
                .parameter(Locale.US)
                .setName("column_id")
                .setType(ParameterType.STRING)
                .setDefaultValue("0001")
                .setImplicit(true)
                .setCanBeBlank(false)
                .setReadonly(true)
                .build(this);

        // when
        StringWriter out = new StringWriter();
        mapper.writer().writeValue(out, expected);

        // then
        assertThat(out.toString(),
                sameJSONAs(IOUtils.toString(this.getClass().getResourceAsStream("textParameter.json"), UTF_8)));
    }

}
