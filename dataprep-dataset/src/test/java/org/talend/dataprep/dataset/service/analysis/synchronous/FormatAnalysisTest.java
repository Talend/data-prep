//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.dataset.service.DataSetServiceTest;
import org.talend.dataprep.schema.csv.CSVFormatFamily;
import org.talend.dataprep.schema.xls.XlsFormatFamily;


public class FormatAnalysisTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgument() {
        formatAnalysis.analyze(null);
    }

    @Test
    public void testNoDataSetFound() {
        String dataSetId = UUID.randomUUID().toString();
        formatAnalysis.analyze(dataSetId);
        assertThat(dataSetMetadataRepository.get(dataSetId), nullValue());
    }

    @Test
    public void testUpdate() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze(id);
        final DataSetMetadata original = dataSetMetadataRepository.get(id);
        final DataSetMetadata modified = dataSetMetadataRepository.get(id);
        modified.setEncoding("windows-1252");
        modified.getContent().getParameters().put("SEPARATOR", ",");

        formatAnalysis.update(original, modified);

        final DataSetMetadata updated = dataSetMetadataRepository.get(id);
        assertNotNull(updated);
        assertThat(updated.getContent().getFormatFamilyId(), is(CSVFormatFamily.BEAN_ID));
        assertThat(updated.getContent().getMediaType(), is("text/csv"));
        assertThat(updated.getEncoding(), is("windows-1252"));
        //assertThat(updated.getContent().getParameters().get("SEPARATOR"), is(";"));

    }

    @Test
    public void testCSVAnalysis() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze(id);
        final DataSetMetadata actual = dataSetMetadataRepository.get(id);
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatFamilyId(), is(CSVFormatFamily.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("text/csv"));
        assertThat(actual.getContent().getParameters().get("SEPARATOR"), is(";"));
    }

    @Test
    public void testEncodingDetection() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../wave_lab_utf16_LE.txt"));
        formatAnalysis.analyze(id);
        final DataSetMetadata actual = dataSetMetadataRepository.get(id);
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatFamilyId(), is(CSVFormatFamily.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("text/csv"));
        assertThat(actual.getContent().getParameters().get("SEPARATOR"), is("\t"));
        assertThat(actual.getEncoding(), is("UTF-16LE"));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-2930.
     */
    @Test
    public void testEncodingDetection_UTF16LE_WithoutBOM() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../utf16_LE_without_bom.txt"));

        formatAnalysis.analyze(id);
        final DataSetMetadata actual = dataSetMetadataRepository.get(id);
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatFamilyId(), is(CSVFormatFamily.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("text/csv"));
        assertThat(actual.getContent().getParameters().get("SEPARATOR"), is(","));
        assertThat(actual.getEncoding(), is("UTF-16LE"));
    }

    @Test
    public void test_TDP_690() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../wave_lab_utf16_LE.txt"));
        formatAnalysis.analyze(id);
        // Test for empty lines
        final DataSetMetadata actual = dataSetMetadataRepository.get(id);
        Stream<DataSetRow> content = contentStore.stream(actual);
        final long emptyRows = content.filter(DataSetRow::isEmpty).count();
        assertThat(emptyRows, is(0L));
    }

    @Test
    public void testXLSXAnalysis() {
        String id = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTest.class.getResourceAsStream("../tagada.xls"));
        formatAnalysis.analyze(id);
        final DataSetMetadata actual = dataSetMetadataRepository.get(id);
        assertThat(actual, notNullValue());
        assertThat(actual.getContent().getFormatFamilyId(), is(XlsFormatFamily.BEAN_ID));
        assertThat(actual.getContent().getMediaType(), is("application/vnd.ms-excel"));
        assertThat(actual.getContent().getParameters().isEmpty(), is(true));
    }

}
