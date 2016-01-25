package org.talend.dataprep.dataset.service.analysis.synchronous;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.schema.csv.CSVFormatGuess;

public class ContentAnalysisTest extends DataSetBaseTest {

    @Autowired
    ContentAnalysis contentAnalysis;

    @Test
    public void testNoDataSetFound() {
        contentAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyDataSetId() {
        contentAnalysis.analyze("");
    }

    @Test
    public void testAnalysisNoHeaderParameter() {
        final DataSetMetadata metadata = metadata().id("1234").build();
        createCsvDataSet(metadata, "5_lines.csv");

        contentAnalysis.analyze(metadata.getId());
        assertThat(dataSetMetadataRepository.get(metadata.getId()), notNullValue());
        final DataSetMetadata actual = dataSetMetadataRepository.get(metadata.getId());
        assertThat(actual.getContent().getNbLinesInHeader(), is(1));
        assertThat(actual.getContent().getNbLinesInFooter(), is(0));
        assertThat(actual.getLifecycle().contentIndexed(), is(true));
    }

    @Test
    public void testAnalysisWithHeaderParameter() {
        final DataSetMetadata metadata = metadata() //
                .id("1234") //
                .parameter(CSVFormatGuess.HEADER_NB_LINES_PARAMETER, "56").build();
        createCsvDataSet(metadata, "5_lines.csv");

        contentAnalysis.analyze(metadata.getId());
        assertThat(dataSetMetadataRepository.get(metadata.getId()), notNullValue());
        final DataSetMetadata actual = dataSetMetadataRepository.get(metadata.getId());
        assertThat(actual.getContent().getNbLinesInHeader(), is(56));
        assertThat(actual.getContent().getNbLinesInFooter(), is(0));
        assertThat(actual.getLifecycle().contentIndexed(), is(true));
    }

    @Test
    public void testAnalysisWithLimit() {
        final DataSetMetadata metadata = metadata().id("3548").build();
        createCsvDataSet(metadata, "100_lines.csv");

        final Long newLimit = 16L;
        final Long originalLimit = (Long) ReflectionTestUtils.getField(contentAnalysis, ContentAnalysis.class, "sizeLimit");
        ReflectionTestUtils.setField(contentAnalysis, "sizeLimit", newLimit);

        contentAnalysis.analyze(metadata.getId());
        ReflectionTestUtils.setField(contentAnalysis, "sizeLimit", originalLimit);

        final DataSetMetadata actual = dataSetMetadataRepository.get(metadata.getId());
        final Optional<Long> limit = actual.getContent().getLimit();
        assertTrue(limit.isPresent());
        assertThat(limit.get(), is(newLimit));
    }

    @Test
    public void testAnalysisWithoutLimit() {
        final DataSetMetadata metadata = metadata().id("8520").build();
        createCsvDataSet(metadata, "5_lines.csv");

        contentAnalysis.analyze(metadata.getId());

        final DataSetMetadata actual = dataSetMetadataRepository.get(metadata.getId());
        assertFalse(actual.getContent().getLimit().isPresent());
    }

    /**
     * Create a dataset out of the given metadata using the given string as source.
     *
     * @param metadata the dataset metadata.
     * @param source the source that points to a csv file within the classpath, relative to this class' package.
     */
    private void createCsvDataSet(DataSetMetadata metadata, String source) {
        metadata.setLocation(new LocalStoreLocation());
        metadata.getContent().setFormatGuessId(CSVFormatGuess.BEAN_ID);
        metadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ",");
        dataSetMetadataRepository.add(metadata);
        contentStore.storeAsRaw(metadata, this.getClass().getResourceAsStream(source));
    }
}
