package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.dataset.DataSetBaseTest;

public class StatisticsAnalysisTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Autowired
    SchemaAnalysis schemaAnalysis;

    @Autowired
    ContentAnalysis contentAnalysis;

    @Autowired
    StatisticsAnalysis statisticsAnalysis;

    /** Random to generate random dataset id. */
    private Random random = new Random();

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-402">https://jira.talendforge.org/browse/TDP-402</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_402() throws Exception {
        final DataSetMetadata metadata = initializeDataSetMetadata(this.getClass().getResourceAsStream("TDP-402.csv"));
        final ColumnMetadata dateOfBirth = metadata.getRowMetadata().getById("0004");
        assertThat(dateOfBirth.getName(), is("date-of-birth"));
        assertThat(dateOfBirth.getType(), is("date"));
        final List<PatternFrequency> patternFrequencies = dateOfBirth.getStatistics().getPatternFrequencies();
        final List<String> patterns = patternFrequencies.stream().map(pf -> pf.getPattern()).collect(Collectors.toList());
        assertThat(patterns.size(), is(7));
        assertTrue(patterns.contains("MM/dd/yyyy"));
        assertTrue(patterns.contains("d/M/yyyy"));
        assertTrue(patterns.contains("d/M/yyyy"));
        assertTrue(patterns.contains("M/d/yyyy"));
        assertTrue(patterns.contains("aaaaa"));
        assertTrue(patterns.contains("yyyy-MM-dd"));
        assertTrue(patterns.contains("yyyy-M-d"));
    }

    /**
     * Initialize a dataset with the given content. Perform the format and the schema analysis.
     *
     * @param content the dataset content.
     * @return the analyzed dataset metadata.
     */
    private DataSetMetadata initializeDataSetMetadata(InputStream content) {
        String id = String.valueOf(random.nextInt(10000));
        final DataSetMetadata metadata = metadata().id(id).build();
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, content);
        formatAnalysis.analyze(id);
        contentAnalysis.analyze(id);
        schemaAnalysis.analyze(id);
        statisticsAnalysis.analyze(id);

        final DataSetMetadata analyzed = dataSetMetadataRepository.get(id);
        assertThat(analyzed.getLifecycle().schemaAnalyzed(), is(true));
        return analyzed;
    }
}
