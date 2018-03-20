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
import org.talend.dataprep.api.dataset.statistics.number.NumberHistogram;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

public class HistogramTest {

    @Test
    public void test_type() throws IOException {
        String json = "{ \"_class\" : \"org.talend.dataprep.api.dataset.statistics.number.NumberHistogram\" , \"items\" : [ { \"occurrences\" : 16 , \"range\" : { \"min\" : 1.0 , \"max\" : 17.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 17.0 , \"max\" : 33.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 33.0 , \"max\" : 49.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 49.0 , \"max\" : 65.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 65.0 , \"max\" : 81.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 81.0 , \"max\" : 97.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 97.0 , \"max\" : 113.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 113.0 , \"max\" : 129.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 129.0 , \"max\" : 145.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 145.0 , \"max\" : 161.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 161.0 , \"max\" : 177.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 177.0 , \"max\" : 193.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 193.0 , \"max\" : 209.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 209.0 , \"max\" : 225.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 225.0 , \"max\" : 241.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 241.0 , \"max\" : 257.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 257.0 , \"max\" : 273.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 273.0 , \"max\" : 289.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 289.0 , \"max\" : 305.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 305.0 , \"max\" : 321.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 321.0 , \"max\" : 337.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 337.0 , \"max\" : 353.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 353.0 , \"max\" : 369.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 369.0 , \"max\" : 385.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 385.0 , \"max\" : 401.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 401.0 , \"max\" : 417.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 417.0 , \"max\" : 433.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 433.0 , \"max\" : 449.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 449.0 , \"max\" : 465.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 465.0 , \"max\" : 481.0}} , { \"occurrences\" : 16 , \"range\" : { \"min\" : 481.0 , \"max\" : 497.0}} , { \"occurrences\" : 4 , \"range\" : { \"min\" : 497.0 , \"max\" : 513.0}}]}";
        json = json.replace("_class", "type").replace("org.talend.dataprep.api.dataset.statistics.number.NumberHistogram", "number").replace("org.talend.dataprep.api.dataset.statistics.number.DateHistogram", "date");


        ObjectMapper mapper = new ObjectMapper();
        NumberHistogram histogram = (NumberHistogram)mapper.readValue(json, Histogram.class);

        assertEquals(histogram.getItems().size(), 32);
    }
}
