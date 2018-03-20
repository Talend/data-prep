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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

/**
 * Unit test for the SelectParameter bean. Mostly test the json serialization.
 *
 * @see SelectParameter
 */
public class SelectParameterTest extends ParameterBaseTest {

    @Test
    public void shouldSerializeToJsonWithItemsInConfiguration() throws IOException {
        // given
        SelectParameter expected = SelectParameter
                .selectParameter(Locale.US) //
                .name("column_id") //
                .defaultValue("") //
                .implicit(false) //
                .canBeBlank(false) //
                .item("first value") //
                .item("2") //
                .item("your choice",
                        Parameter
                                .parameter(Locale.US)
                                .setName("limit")
                                .setType(ParameterType.INTEGER)
                                .setDefaultValue(StringUtils.EMPTY)
                                .setCanBeBlank(false)
                                .build(this)) //
                .build(this);

        // when
        StringWriter out = new StringWriter();
        mapper.writer().writeValue(out, expected);

        // then
        assertThat(out.toString(),
                sameJSONAs(IOUtils.toString(this.getClass().getResourceAsStream("selectParameter.json"), UTF_8)));
    }

    @Test
    public void shouldCreateConstantItem() {
        // when
        final SelectParameter params = SelectParameter.selectParameter(Locale.US).constant("key", "a constant key").build(this);

        // then
        assertThat(params.getItems().get(0).getValue(), is("key"));
        assertThat(params.getItems().get(0).getLabel(), is("a constant key"));
    }

}
