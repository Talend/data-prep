package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetBaseTest;

public class ContentAnalyzerTest extends DataSetBaseTest {

    @Autowired
    ContentAnalysis contentAnalysis;

    @Test
    public void testNoDataSetFound() throws Exception {
        contentAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        dataSetMetadataRepository.add(metadata().id("1234").build());
        contentAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), notNullValue());
        final DataSetMetadata metadata = dataSetMetadataRepository.get("1234");
        assertThat(metadata.getContent().getNbLinesInHeader(), is(1));
        assertThat(metadata.getContent().getNbLinesInFooter(), is(0));
        assertThat(metadata.getLifecycle().contentIndexed(), is(true));
    }

}
