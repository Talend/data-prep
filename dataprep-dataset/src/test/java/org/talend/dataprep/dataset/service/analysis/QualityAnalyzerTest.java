package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.dataset.Application;
import org.talend.dataprep.dataset.service.DataSetServiceTests;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class QualityAnalyzerTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Autowired
    QualityAnalysis qualityAnalysis;

    @Autowired
    SchemaAnalysis schemaAnalysis;

    @Autowired
    @Qualifier("ContentStore#local")
    DataSetContentStore contentStore;

    @Autowired
    DataSetMetadataRepository repository;

    @After
    public void tearDown() throws Exception {
        repository.clear();
        contentStore.clear();
    }

    @Test
    public void testNoDataSetFound() throws Exception {
        qualityAnalysis.analyze("1234");
        assertThat(repository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze("1234");
        schemaAnalysis.analyze("1234");
        // Analyze quality
        qualityAnalysis.analyze("1234");
        assertThat(metadata.getLifecycle().qualityAnalyzed(), is(true));
        assertThat(metadata.getContent().getNbRecords(), is(5));
        for (ColumnMetadata column : metadata.getRow().getColumns()) {
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
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../dataset_with_invalid_records.csv"));
        formatAnalysis.analyze(dsId);
        schemaAnalysis.analyze(dsId);
        // Analyze quality
        qualityAnalysis.analyze(dsId);
        assertThat(metadata.getLifecycle().qualityAnalyzed(), is(true));
        assertThat(metadata.getContent().getNbRecords(), is(9));
        assertThat(metadata.getRow().getColumns().size(), is(2));
        ColumnMetadata secondColumn = metadata.getRow().getColumns().get(1);
        Quality quality = secondColumn.getQuality();
        assertThat(quality.getValid(), is(6));
        assertThat(quality.getInvalid(), is(2));
        assertThat(quality.getEmpty(), is(1));

        Set<String> expectedInvalidValues = new HashSet<>(1);
        expectedInvalidValues.add("N/A");
        assertThat(quality.getInvalidValues(), is(expectedInvalidValues));

    }
}
