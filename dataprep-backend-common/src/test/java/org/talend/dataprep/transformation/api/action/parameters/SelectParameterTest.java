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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.talend.dataprep.parameters.*;

/**
 * Unit test for the SelectParameter bean. Mostly test the json serialization.
 *
 * @see SelectParameter
 */
public class SelectParameterTest extends ParameterBaseTest {

    @Test
    public void shouldSerializeToJsonWithItemsInConfiguration() throws IOException {
        // given
        SelectParameter expected = SelectParameter.Builder.builder() //
                .name("column_id") //
                .defaultValue("") //
                .implicit(false) //
                .canBeBlank(false) //
                .item("first value") //
                .item("2") //
                .item("your choice", new Parameter("limit", ParameterType.INTEGER, StringUtils.EMPTY, false, false)) //
                .build();

        // when
        StringWriter out = new StringWriter();
        mapper.writer().writeValue(out, expected);

        // then
        assertThat(out.toString(), sameJSONAs(IOUtils.toString(this.getClass().getResourceAsStream("selectParameter.json"))));
    }

    @Test
    public void shouldCreateLocalizedItem() {
        // when
        final SelectParameter params = SelectParameter.Builder.builder().item("key", "key").build();

        // then
        assertThat(params.getItems().get(0), IsInstanceOf.instanceOf(LocalizedItem.class));
    }

    @Test
    public void shouldCreateLocalizedItemWithInlineParameters() {
        // when
        final SelectParameter params = SelectParameter
                .Builder
                .builder()
                .item("key", "key", new Parameter())
                .build();

        // then
        assertThat(params.getItems().get(0), IsInstanceOf.instanceOf(LocalizedItem.class));
    }

    @Test
    public void shouldCreateConstantItem() {
        // when
        final SelectParameter params = SelectParameter
                .Builder
                .builder()
                .constant("key", "a constant key")
                .build();

        // then
        assertThat(params.getItems().get(0), IsInstanceOf.instanceOf(TextItem.class));
        assertThat(params.getItems().get(0).getValue(), is("key"));
        assertThat(params.getItems().get(0).getLabel(), is("a constant key"));
    }

}
