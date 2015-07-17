package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.Application;
import org.talend.dataprep.dataset.service.DataSetServiceTests;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class SchemaAnalyzerTest {

    @Autowired
    FormatAnalysis formatAnalysis;

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
        schemaAnalysis.analyze("1234");
        assertThat(repository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../avengers.csv"));
        formatAnalysis.analyze("1234");
        // Analyze schema
        schemaAnalysis.analyze("1234");
        assertThat(metadata.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "nickname", "secret firstname", "secret lastname", "date of birth", "city" };
        Type[] expectedTypes = { Type.STRING, Type.STRING, Type.STRING, Type.DATE, Type.STRING };
        int i = 0;
        int j = 0;
        for (ColumnMetadata column : metadata.getRow().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i++]));
            assertThat(column.getType(), is(expectedTypes[j++].getName()));
        }
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-224">https://jira.talendforge.org/browse/TDP-224</a>.
     * 
     * @throws Exception
     */
    @Test
    public void testTDP_224() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../whatever.xls"));
        formatAnalysis.analyze("1234");
        // Analyze schema
        schemaAnalysis.analyze("1234");
        assertThat(metadata.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "whaterver" }; // Not a typo: this is what QA provided as column name.
        Type[] expectedTypes = { Type.STRING };
        int i = 0;
        int j = 0;
        for (ColumnMetadata column : metadata.getRow().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i++]));
            assertThat(column.getType(), is(expectedTypes[j++].getName()));
        }
    }

    @Test
    public void testGenderAnalysis() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../gender.csv"));
        formatAnalysis.analyze("1234");
        // Analyze schema
        schemaAnalysis.analyze("1234");
        assertThat(metadata.getLifecycle().schemaAnalyzed(), is(true));
        // Gender must be a String with Gender domain
        String[] expectedNames = { "name", "bounty", "gender" };
        Type[] expectedTypes = { Type.STRING, Type.INTEGER, Type.STRING };
        String[] expectedDomains = { "FIRSTNAME", "", "GENDER" };
        int i = 0;
        for (ColumnMetadata column : metadata.getRow().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            assertThat(column.getDomain(), is(expectedDomains[i]));
            i++;
        }
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-226">https://jira.talendforge.org/browse/TDP-226</a>.
     * 
     * @throws Exception
     */
    @Test
    public void testTDP_226() throws Exception {
        final DataSetMetadata metadata = metadata().id("1234").build();
        repository.add(metadata);
        contentStore.storeAsRaw(metadata, DataSetServiceTests.class.getResourceAsStream("../empty_lines.csv"));
        formatAnalysis.analyze("1234");
        // Analyze schema
        schemaAnalysis.analyze("1234");
        assertThat(metadata.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "id", "firstname", "lastname", "age", "date-of-birth", "alive" };
        Type[] expectedTypes = { Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER, Type.DATE, Type.BOOLEAN };
        int i = 0;
        for (ColumnMetadata column : metadata.getRow().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            i++;
        }
    }
}
