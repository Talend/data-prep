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
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.parameters.ColumnParameter;

/**
 * Unit test for the ColumnParameter bean. Mostly test the json serialization.
 * 
 * @see ColumnParameter
 */
public class ColumnParameterTest extends ParameterBaseTest {

    @Test
    public void shouldSerializeToJsonWithFilterAttributeInConfiguration() throws IOException {
        // given
        ColumnParameter expected = new ColumnParameter("column_id", "0001", false, false, Arrays.asList("numeric", "string"));

        // when
        StringWriter out = new StringWriter();
        builder.build().writer().writeValue(out, expected);

        // then
        assertThat(out.toString(), sameJSONAs(IOUtils.toString(this.getClass().getResourceAsStream("columnParameter.json"))));
    }
}