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

package org.talend.dataprep.transformation.aggregation.api;

import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.aggregation.operation.NumberContext;

/**
 * Unit test for the aggregation result json serialization.
 */
public class AggregationResultTest extends TransformationBaseTest {

    /** The data-prep ready jackson module. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Test
    public void shouldSerialize() throws IOException {

        // given
        AggregationResult result = new AggregationResult(Operator.MAX);
        result.put("toto", new NumberContext(123d));
        result.put("titi", new NumberContext(456d));
        result.put("tata", new NumberContext(789d));
        result.put("tutu", new NumberContext(753d));

        // when
        String actual = builder.build().writer().writeValueAsString(result);

        // then
        Assert.assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("aggregation_result.json")));
    }

}