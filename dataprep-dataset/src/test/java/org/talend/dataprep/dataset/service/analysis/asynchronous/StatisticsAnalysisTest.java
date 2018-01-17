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

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
import org.talend.dataprep.dataset.event.DataSetImportedEvent;
import org.talend.dataprep.dataset.service.analysis.synchronous.ContentAnalysis;
import org.talend.dataprep.dataset.service.analysis.synchronous.FormatAnalysis;
import org.talend.dataprep.dataset.service.analysis.synchronous.SchemaAnalysis;

public class StatisticsAnalysisTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Autowired
    SchemaAnalysis schemaAnalysis;

    @Autowired
    ContentAnalysis contentAnalysis;

    @Autowired
    SyncBackgroundAnalysis statisticsAnalysis;

    /** Random to generate random dataset id. */
    private Random random = new Random();

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-402">https://jira.talendforge.org/browse/TDP-402</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_402() throws Exception {
        final DataSetMetadata metadata = initializeDataSetMetadata(this.getClass().getResourceAsStream("dataset.csv"));
        final ColumnMetadata dateOfBirth = metadata.getRowMetadata().getById("0004");
        assertThat(dateOfBirth.getName(), is("date-of-birth"));
        assertThat(dateOfBirth.getType(), is("date"));
        final List<PatternFrequency> patternFrequencies = dateOfBirth.getStatistics().getPatternFrequencies();
        final List<String> patterns = patternFrequencies.stream().map(pf -> pf.getPattern()).collect(Collectors.toList());
        assertThat(patterns.size(), is(5));
        assertTrue(patterns.contains("MM/dd/yyyy"));
        assertTrue(patterns.contains("dd/MM/yyyy"));
        assertTrue(patterns.contains("aaaaa"));
        assertTrue(patterns.contains("yyyy-MM-dd"));
        assertTrue(patterns.contains("yyyy-M-d"));
    }

    @Test
    public void should_update_dataset_nb_records() throws Exception {
        //given
        final InputStream dataset = this.getClass().getResourceAsStream("dataset.csv");

        //when
        final DataSetMetadata metadata = initializeDataSetMetadata(dataset);

        //then
        assertThat(metadata.getContent().getNbRecords(), is(4L));
    }

    /**
     * Initialize a dataset with the given content. Perform the format and the schema analysis.
     *
     * @param content the dataset content.
     * @return the analyzed dataset metadata.
     */
    private DataSetMetadata initializeDataSetMetadata(InputStream content) {
        String id = String.valueOf(random.nextInt(10000));
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
        dataSetMetadataRepository.save(metadata);
        contentStore.storeAsRaw(metadata, content);
        formatAnalysis.analyze(id);
        contentAnalysis.analyze(id);
        schemaAnalysis.analyze(id);
        statisticsAnalysis.onApplicationEvent(new DataSetImportedEvent(id));

        final DataSetMetadata analyzed = dataSetMetadataRepository.get(id);
        assertThat(analyzed.getLifecycle().schemaAnalyzed(), is(true));
        return analyzed;
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-2120">TDP-2120</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_2021() throws Exception {
        final DataSetMetadata metadata = initializeDataSetMetadata(this.getClass().getResourceAsStream("dataset.csv"));
        final ColumnMetadata dateOfBirth = metadata.getRowMetadata().getById("0004");
        assertThat(dateOfBirth.getName(), is("date-of-birth"));
        final double min = dateOfBirth.getStatistics().getMin();
        final double max = dateOfBirth.getStatistics().getMax();
        assertFalse(Double.isNaN(min));
        assertFalse(Double.isNaN(max));
        assertEquals(-924912000000D, min, 0.5);
        assertEquals(-707529600000D, max, 0.5);
    }
}
