//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.location.HttpLocation;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.SchemaParserResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DataSetMetadataBuilderTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public class DataSetMetadataBuilderTest {

    @Autowired
    private DataSetMetadataBuilder builder;

    @Test(expected = IllegalStateException.class)
    public void testExpectedId() {
        builder.metadata().build();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseOfMetadata() {
        builder.id("12").build();
    }

    @Test
    public void testId() throws Exception {
        assertEquals("1234", builder.metadata().id("1234").build().getId());
    }

    @Test
    public void testAuthor() throws Exception {
        assertEquals("author", builder.metadata().id("1234").author("author").build().getAuthor());
    }

    @Test
    public void testName() throws Exception {
        assertEquals("name", builder.metadata().id("1234").name("name").build().getName());
    }

    @Test
    public void testVersion() throws Exception {
        // default version
        assertNotNull(builder.metadata().id("4321").build().getAppVersion());
        // set version
        assertEquals("1.0.PE", builder.metadata().id("4321").appVersion("1.0.PE").build().getAppVersion());
    }

    @Test
    public void testCreated() throws Exception {
        assertEquals(1234, builder.metadata().id("1234").created(1234).build().getCreationDate());
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(30000, builder.metadata().id("1234").size(30000).build().getContent().getNbRecords());
    }

    @Test
    public void testHeaderSize() throws Exception {
        assertEquals(1, builder.metadata().id("1234").headerSize(1).build().getContent().getNbLinesInHeader());
    }

    @Test
    public void testFooterSize() throws Exception {
        assertEquals(1, builder.metadata().id("1234").footerSize(1).build().getContent().getNbLinesInFooter());
    }

    @Test
    public void testContentAnalyzed() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234").contentAnalyzed(false).build();
        assertEquals(false, metadata.getLifecycle().contentIndexed());
        metadata.getLifecycle().contentIndexed(true);
        assertEquals(true, metadata.getLifecycle().contentIndexed());
    }

    @Test
    public void testSchemaAnalyzed() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234").schemaAnalyzed(false).build();
        assertEquals(false, metadata.getLifecycle().schemaAnalyzed());
        metadata.getLifecycle().schemaAnalyzed(true);
        assertEquals(true, metadata.getLifecycle().schemaAnalyzed());
    }

    @Test
    public void testQualityAnalyzed() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234").qualityAnalyzed(false).build();
        assertEquals(false, metadata.getLifecycle().qualityAnalyzed());
        metadata.getLifecycle().qualityAnalyzed(true);
        assertEquals(true, metadata.getLifecycle().qualityAnalyzed());
    }

    @Test
    public void testImporting() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234").importing(true).build();
        assertEquals(true, metadata.getLifecycle().importing());
    }

    @Test
    public void testSheetName() throws Exception {
        assertEquals("test", builder.metadata().id("1234").sheetName("test").build().getSheetName());
    }

    @Test
    public void testDraft() throws Exception {
        assertEquals(true, builder.metadata().id("1234").draft(true).build().isDraft());
    }

    @Test
    public void testFormatGuessId() throws Exception {
        assertEquals("formatGuess",
                builder.metadata().id("1234").formatGuessId("formatGuess").build().getContent().getFormatGuessId());
    }

    @Test
    public void testMediaType() throws Exception {
        assertEquals("mediaType", builder.metadata().id("1234").mediaType("mediaType").build().getContent().getMediaType());
    }

    @Test
    public void testIsFavorite() throws Exception {
        assertEquals(true, builder.metadata().id("1234").isFavorite(true).build().isFavorite());
    }

    @Test
    public void testLocation() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234").location(new HttpLocation()).build();
        assertEquals(HttpLocation.class, metadata.getLocation().getClass());
    }

    @Test
    public void testCertificationStep() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234")
                .certificationStep(DataSetGovernance.Certification.CERTIFIED).build();
        assertEquals(DataSetGovernance.Certification.CERTIFIED, metadata.getGovernance().getCertificationStep());
    }

    @Test
    public void testCopy() throws Exception {
        final DataSetMetadata build = builder.metadata() //
                .id("1234") //
                .row(column().type(Type.STRING).name("col0"), column().type(Type.STRING).name("col1")) //
                .build();
        final DataSetMetadata copy = builder.metadata().copy(build).build();
        assertEquals("1234", copy.getId());
        assertEquals("col0", copy.getRowMetadata().getColumns().get(0).getName());
        assertEquals("col1", copy.getRowMetadata().getColumns().get(1).getName());
    }

    @Test
    public void testSchemaParserResult() throws Exception {
        final DataSetMetadata metadata = builder.metadata().id("1234")
                .schemaParserResult(SchemaParserResult.Builder.parserResult().sheetName("sheetName").build()).build();
        assertEquals("sheetName", metadata.getSchemaParserResult().getSheetName());
    }
}