package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.Application;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ContentAnalyzerTest {

    @Autowired
    ContentAnalysis contentAnalysis;

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
        contentAnalysis.analyze("1234");
        assertThat(repository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        repository.add(metadata().id("1234").build());
        contentAnalysis.analyze("1234");
        assertThat(repository.get("1234"), notNullValue());
        final DataSetMetadata metadata = repository.get("1234");
        assertThat(metadata.getContent().getNbLinesInHeader(), is(1));
        assertThat(metadata.getContent().getNbLinesInFooter(), is(0));
        assertThat(metadata.getLifecycle().contentIndexed(), is(true));
    }

}
