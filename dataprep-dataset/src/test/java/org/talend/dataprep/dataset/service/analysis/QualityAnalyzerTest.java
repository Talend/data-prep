package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.dataset.service.DataSetServiceTests;

public class QualityAnalyzerTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Autowired
    QualityAnalysis qualityAnalysis;

    @Autowired
    SchemaAnalysis schemaAnalysis;

    @Test
    public void testNoDataSetFound() throws Exception {
        qualityAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze("1234");
        schemaAnalysis.analyze("1234");
        // Analyze quality
        qualityAnalysis.analyze("1234");
        final DataSetMetadata actual = dataSetMetadataRepository.get("1234");
        assertThat(actual.getLifecycle().qualityAnalyzed(), is(true));
        assertThat(actual.getContent().getNbRecords(), is(5));
        for (ColumnMetadata column : actual.getRow().getColumns()) {
            final Quality quality = column.getQuality();
            assertThat(quality.getValid(), is(5));
            assertThat(quality.getInvalid(), is(0));
            assertThat(quality.getEmpty(), is(0));
        }
    }

    @Test
    public void testAnalysisWithInvalidValues() throws Exception {
        String dsId = "4321";
        final DataSetMetadata metadata = metadata().id(dsId).build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../dataset_with_invalid_records.csv"));
        formatAnalysis.analyze(dsId);
        schemaAnalysis.analyze(dsId);
        // Analyze quality
        qualityAnalysis.analyze(dsId);
        final DataSetMetadata actual = dataSetMetadataRepository.get(dsId);
        assertThat(actual.getLifecycle().qualityAnalyzed(), is(true));
        assertThat(actual.getContent().getNbRecords(), is(9));
        assertThat(actual.getRow().getColumns().size(), is(2));
        ColumnMetadata secondColumn = actual.getRow().getColumns().get(1);
        Quality quality = secondColumn.getQuality();
        assertThat(quality.getValid(), is(6));
        assertThat(quality.getInvalid(), is(2));
        assertThat(quality.getEmpty(), is(1));

        Set<String> expectedInvalidValues = new HashSet<>(1);
        expectedInvalidValues.add("N/A");
        assertThat(quality.getInvalidValues(), is(expectedInvalidValues));

    }
}
