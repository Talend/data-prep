/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package org.talend.dataprep.api.dataset.statistics;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class TextLengthSummaryTest {

    @Test
    public void test_deserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        String json = "{\"minimalLength\":NaN,\"maximalLength\":NaN,\"averageLength\":NaN}";
        TextLengthSummary textLengthSummary = mapper.readValue(json, TextLengthSummary.class);

        assertEquals(textLengthSummary.getAverageLength(), Double.NaN, 0.0);
    }
}
