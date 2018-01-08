// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.service.analysis.synchronous;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Random;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.dataset.service.DataSetServiceTest;

public class SchemaAnalysisTest extends DataSetBaseTest {

    @Autowired
    FormatAnalysis formatAnalysis;

    @Autowired
    SchemaAnalysis schemaAnalysis;

    @Autowired
    ContentAnalysis contentAnalysis;

    /** Random to generate random dataset id. */
    private Random random = new Random();

    @Test
    public void testNoDataSetFound() throws Exception {
        schemaAnalysis.analyze("1234");
        assertThat(dataSetMetadataRepository.get("1234"), nullValue());
    }

    @Test
    public void testAnalysis() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(DataSetServiceTest.class.getResourceAsStream("../avengers.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "nickname", "secret firstname", "secret lastname", "date of birth", "city" };
        Type[] expectedTypes = { Type.STRING, Type.STRING, Type.STRING, Type.DATE, Type.STRING };
        int i = 0;
        int j = 0;
        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
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
        final DataSetMetadata actual = initializeDataSetMetadata(DataSetServiceTest.class.getResourceAsStream("../whatever.xls"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "whaterver" }; // Not a typo: this is what QA provided as column name.
        Type[] expectedTypes = { Type.STRING };
        int i = 0;
        int j = 0;
        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i++]));
            assertThat(column.getType(), is(expectedTypes[j++].getName()));
        }
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-279">https://jira.talendforge.org/browse/TDP-279</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_279() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(
                DataSetServiceTest.class.getResourceAsStream("../post_code.xls"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "zip" };
        Type[] expectedTypes = { Type.INTEGER };
        String[] expectedDomains = { "FR_POSTAL_CODE" };
        int i = 0;

        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            assertThat(column.getDomain(), is(expectedDomains[i++]));
            assertThat(column.getSemanticDomains()).isNotNull().isNotEmpty().hasSize(4).contains(
                    new SemanticDomain("FR_POSTAL_CODE", "FR Postal Code", (float) 58.33), //
                    new SemanticDomain("FR_CODE_COMMUNE_INSEE", "FR Insee Code", (float) 58.33), //
                    new SemanticDomain("DE_POSTAL_CODE", "DE Postal Code", (float) 58.33), //
                    new SemanticDomain("US_POSTAL_CODE", "US Postal Code", (float) 58.33));
        }
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-471">https://jira.talendforge.org/browse/TDP-471</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_471() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(
                DataSetServiceTest.class.getResourceAsStream("../semantic_type_threshold.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "gender_column" };
        Type[] expectedTypes = { Type.INTEGER };
        String[] expectedDomains = { "" };
        int i = 0;

        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            assertThat(column.getDomain(), is(expectedDomains[i++]));
            assertThat(column.getSemanticDomains()).isNotNull().isNotEmpty().hasSize(2).contains(
                    new SemanticDomain("GENDER", "Gender", (float) 35), //
                    new SemanticDomain("CIVILITY", "Civility", (float) 20.833334));
        }
    }

    @Test
    public void testGenderAnalysis() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(DataSetServiceTest.class.getResourceAsStream("../gender.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        // Gender must be a String with Gender domain
        String[] expectedNames = { "name", "bounty", "gender" };
        Type[] expectedTypes = { Type.STRING, Type.INTEGER, Type.STRING };
        String[] expectedDomains = { "FIRST_NAME", "", "GENDER" };
        int i = 0;
        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
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
        final DataSetMetadata actual = initializeDataSetMetadata(
                DataSetServiceTest.class.getResourceAsStream("../empty_lines.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "id", "firstname", "lastname", "age", "date-of-birth", "alive" };
        Type[] expectedTypes = { Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER, Type.DATE, Type.BOOLEAN };
        int i = 0;
        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            i++;
        }
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
        // Analyze schema
        schemaAnalysis.analyze(id);

        final DataSetMetadata analyzed = dataSetMetadataRepository.get(id);
        assertNotNull(analyzed);
        assertNotNull(analyzed.getLifecycle());
        assertThat(analyzed.getLifecycle().schemaAnalyzed(), is(true));
        return analyzed;
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-1674">TDP-1674_error_with_ipv6_addresses</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_1674() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(DataSetServiceTest.class.getResourceAsStream("../ipv6.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        String[] expectedNames = { "number", "description", "address" };
        Type[] expectedTypes = { Type.INTEGER, Type.STRING, Type.STRING };
        int i = 0;
        for (ColumnMetadata column : actual.getRowMetadata().getColumns()) {
            assertThat(column.getName(), is(expectedNames[i]));
            assertThat(column.getType(), is(expectedTypes[i].getName()));
            i++;
        }

        assertThat("IPv6 Address", is(actual.getRowMetadata().getColumns().get(2).getDomainLabel()));
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-855">TDP-855_movie_title_detected_as_city</a>.
     *
     * @throws Exception
     */
    @Test
    public void testTDP_855() throws Exception {
        final DataSetMetadata actual = initializeDataSetMetadata(
                DataSetServiceTest.class.getResourceAsStream("../TDP-855_movie_title_detected_as_city.csv"));
        assertThat(actual.getLifecycle().schemaAnalyzed(), is(true));
        ColumnMetadata column = actual.getRowMetadata().getColumns().get(0);
        String expectedName = "Genre: (Movie, Program, show)";
        Type expectedType = Type.STRING;

        assertThat(expectedName, is(column.getName()));
        assertThat(expectedType.getName(), is(column.getType()));
        assertThat("", is(column.getDomainLabel()));
    }
}
