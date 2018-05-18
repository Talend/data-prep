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

package org.talend.dataprep.transformation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.api.dataset.DataSet;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
public class DataSetDeserializationTest {

    @Test
    public void testDeserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(
                "{\"records\":[{\"0000\":\"yves\",\"0001\":\"20\",\"0002\":\"back\",\"0003\":\"30\",\"tdpId\":1},{\"0000\":\"stéphane\",\"0001\":\"19\",\"0002\":\"back\",\"0003\":\"48\",\"tdpId\":2}],\"metadata\":{\"columns\":[{\"name\":\"Name\",\"headerSize\":0,\"type\":\"string\",\"quality\":{\"empty\":0,\"invalid\":0,\"valid\":2},\"id\":\"0000\",\"statistics\":{\"count\":2,\"valid\":2,\"invalid\":0,\"empty\":0,\"max\":\"NaN\",\"min\":\"NaN\",\"mean\":\"NaN\",\"variance\":\"NaN\",\"duplicateCount\":0,\"distinctCount\":2,\"frequencyTable\":[{\"data\":\"yves\",\"occurrences\":1},{\"data\":\"stéphane\",\"occurrences\":1}],\"patternFrequencyTable\":[{\"pattern\":\"aaaaaaaa\",\"occurrences\":1},{\"pattern\":\"aaaa\",\"occurrences\":1}],\"quantiles\":{\"median\":\"NaN\",\"lowerQuantile\":\"NaN\",\"upperQuantile\":\"NaN\"},\"textLengthSummary\":{\"minimalLength\":4.0,\"maximalLength\":8.0,\"averageLength\":6.0}},\"domain\":\"FIRST_NAME\",\"domainLabel\":\"First Name\",\"domainFrequency\":100.0,\"semanticDomains\":[{\"id\":\"FIRST_NAME\",\"label\":\"First Name\",\"frequency\":100.0},{\"id\":\"FR_COMMUNE\",\"label\":\"FR Commune\",\"frequency\":50.0}],\"domainForced\":false,\"typeForced\":false},{\"name\":\"Score\",\"headerSize\":0,\"type\":\"integer\",\"quality\":{\"empty\":0,\"invalid\":0,\"valid\":2},\"id\":\"0001\",\"statistics\":{\"count\":2,\"valid\":2,\"invalid\":0,\"empty\":0,\"max\":20.0,\"min\":19.0,\"mean\":19.5,\"variance\":0.5,\"duplicateCount\":0,\"distinctCount\":2,\"frequencyTable\":[{\"data\":\"19\",\"occurrences\":1},{\"data\":\"20\",\"occurrences\":1}],\"patternFrequencyTable\":[{\"pattern\":\"99\",\"occurrences\":2}],\"quantiles\":{\"median\":19.5,\"lowerQuantile\":19.0,\"upperQuantile\":20.0},\"histogram\":{\"type\":\"number\",\"items\":[{\"occurrences\":1,\"range\":{\"min\":19.0,\"max\":19.0}},{\"occurrences\":1,\"range\":{\"min\":20.0,\"max\":20.0}}]},\"textLengthSummary\":{\"minimalLength\":\"NaN\",\"maximalLength\":\"NaN\",\"averageLength\":\"NaN\"}},\"domain\":\"\",\"domainLabel\":\"\",\"domainFrequency\":0.0,\"semanticDomains\":[],\"domainForced\":false,\"typeForced\":false},{\"name\":\"Team\",\"headerSize\":0,\"type\":\"string\",\"quality\":{\"empty\":0,\"invalid\":0,\"valid\":2},\"id\":\"0002\",\"statistics\":{\"count\":2,\"valid\":2,\"invalid\":0,\"empty\":0,\"max\":\"NaN\",\"min\":\"NaN\",\"mean\":\"NaN\",\"variance\":\"NaN\",\"duplicateCount\":1,\"distinctCount\":1,\"frequencyTable\":[{\"data\":\"back\",\"occurrences\":2}],\"patternFrequencyTable\":[{\"pattern\":\"aaaa\",\"occurrences\":2}],\"quantiles\":{\"median\":\"NaN\",\"lowerQuantile\":\"NaN\",\"upperQuantile\":\"NaN\"},\"textLengthSummary\":{\"minimalLength\":4.0,\"maximalLength\":4.0,\"averageLength\":4.0}},\"domain\":\"LAST_NAME\",\"domainLabel\":\"Last Name\",\"domainFrequency\":100.0,\"semanticDomains\":[{\"id\":\"LAST_NAME\",\"label\":\"Last Name\",\"frequency\":100.0}],\"domainForced\":false,\"typeForced\":false},{\"name\":\"Birth\",\"headerSize\":0,\"type\":\"integer\",\"quality\":{\"empty\":0,\"invalid\":0,\"valid\":2},\"id\":\"0003\",\"statistics\":{\"count\":2,\"valid\":2,\"invalid\":0,\"empty\":0,\"max\":48.0,\"min\":30.0,\"mean\":39.0,\"variance\":162.0,\"duplicateCount\":0,\"distinctCount\":2,\"frequencyTable\":[{\"data\":\"48\",\"occurrences\":1},{\"data\":\"30\",\"occurrences\":1}],\"patternFrequencyTable\":[{\"pattern\":\"99\",\"occurrences\":2}],\"quantiles\":{\"median\":39.0,\"lowerQuantile\":30.0,\"upperQuantile\":48.0},\"histogram\":{\"type\":\"number\",\"items\":[{\"occurrences\":1,\"range\":{\"min\":30.0,\"max\":30.0}},{\"occurrences\":1,\"range\":{\"min\":48.0,\"max\":48.0}}]},\"textLengthSummary\":{\"minimalLength\":\"NaN\",\"maximalLength\":\"NaN\",\"averageLength\":\"NaN\"}},\"domain\":\"\",\"domainLabel\":\"\",\"domainFrequency\":0.0,\"semanticDomains\":[],\"domainForced\":false,\"typeForced\":false}]}}");

        assertEquals(2, dataSet.getRecords().count());
        assertNotNull(dataSet.getMetadata());
    }

}
