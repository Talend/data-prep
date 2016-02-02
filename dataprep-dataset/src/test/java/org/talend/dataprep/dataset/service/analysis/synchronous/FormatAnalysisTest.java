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

package org.talend.dataprep.dataset.service.analysis.synchronous;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.dataset.service.DataSetServiceTests;
import org.talend.dataprep.schema.csv.CSVFormatGuess;
import org.talend.dataprep.schema.xls.XlsFormatGuess;


public class FormatAnalysisTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Test
    public void testNoDataSetFound() throws Exception {
        formatAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), nullValue());
    }

    @Test
    public void testCSVAnalysis() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("1234").build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze("1234");
        final DataSetMetadata actual = dataSetMetadataRepository.get("1234");
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatGuessId(), is(CSVFormatGuess.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("text/csv"));
        assertThat(actual.getContent().getParameters().get("SEPARATOR"), is(";"));
    }

    @Test
    public void testEncodingDetection() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("1234").build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../wave_lab_utf16_LE.txt"));
        formatAnalysis.analyze("1234");
        final DataSetMetadata actual = dataSetMetadataRepository.get("1234");
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatGuessId(), is(CSVFormatGuess.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("text/csv"));
        assertThat(actual.getContent().getParameters().get("SEPARATOR"), is("\t"));
        assertThat(actual.getEncoding(), is("UTF-16"));
    }

    @Test
    public void test_TDP_690() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("1234").build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../wave_lab_utf16_LE.txt"));
        formatAnalysis.analyze("1234");
        // Test for empty lines
        final DataSetMetadata actual = dataSetMetadataRepository.get("1234");
        Stream<DataSetRow> content = contentStore.stream(actual);
        final long emptyRows = content.filter(DataSetRow::isEmpty).count();
        assertThat(emptyRows, is(0L));
    }

    @Test
    public void testXLSXAnalysis() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("1234").build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../tagada.xls"));
        formatAnalysis.analyze("1234");
        final DataSetMetadata actual = dataSetMetadataRepository.get("1234");
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatGuessId(), is(XlsFormatGuess.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("application/vnd.ms-excel"));
        assertThat(actual.getContent().getParameters().isEmpty(), is(true));
    }

}
