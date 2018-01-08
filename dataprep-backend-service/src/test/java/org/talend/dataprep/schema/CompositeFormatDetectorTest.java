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

package org.talend.dataprep.schema;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.html.HtmlEncodingDetector;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.html.HtmlFormatFamily;
import org.talend.dataprep.schema.xls.XlsFormatFamily;

public class CompositeFormatDetectorTest extends AbstractSchemaTestUtils {

    /** The format guesser to test. */
    @Autowired
    CompositeFormatDetector formatDetector;

    /**
     * Text file
     */
    @Test
    public void should_detect_unsupported_format_family() throws IOException {
        Format actual = formatDetector.detect(new ByteArrayInputStream(new byte[1]));
        Assert.assertNotNull(actual);
        assertTrue(actual.getFormatFamily() instanceof UnsupportedFormatFamily);
    }

    @Test
    public void should_detect_at_least_a_format() throws IOException {
        Format actual = formatDetector.detect(new ByteArrayInputStream(new byte[1]));
        Assert.assertNotNull(actual);
        assertTrue(actual.getFormatFamily() instanceof UnsupportedFormatFamily);
    }

    /**
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void should_not_read_null_input_stream() throws Exception {
        formatDetector.detect(null);
    }

    @Test
    public void should_detect_old_xls_format() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("xls/test.xls")) {
            Format actual = formatDetector.detect(inputStream);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }
    }

    @Test
    public void should_detect_new_xls_format() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("xls/test_new.xlsx")) {
            Format actual = formatDetector.detect(inputStream);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }
    }

    @Test
    public void read_xls_that_can_be_parsed_as_csv_TDP_375() throws Exception {

        String fileName = "xls/TDP-375_xsl_read_as_csv.xls";

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            Format actual = formatDetector.detect(inputStream);
            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertEquals(XlsFormatFamily.MEDIA_TYPE, actual.getFormatFamily().getMediaType());
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }

    }

    @Test
    public void guess_html_format_success() throws Exception {

        String fileName = "html/sales-force.xls";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Charset charset = new HtmlEncodingDetector().detect(this.getClass().getResourceAsStream(fileName), new Metadata());
        Format actual = formatDetector.detect(this.getClass().getResourceAsStream(fileName));

        assertTrue(actual.getFormatFamily() instanceof HtmlFormatFamily);
        assertTrue(StringUtils.equals("UTF-16", actual.getEncoding()));
    }

    @Test
    public void guess_html_format_fail() throws Exception {

        String fileName = "html/foo.html";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Format actual = formatDetector.detect(this.getClass().getResourceAsStream(fileName));
        assertFalse(actual.getFormatFamily() instanceof HtmlFormatFamily);
    }

    @Test
    public void should_not_detect_an_ods_document() throws Exception {

        String fileName = "200-titres-les-plus-aides-2012.ods";

        Format actual = formatDetector.detect(this.getClass().getResourceAsStream(fileName));
        assertTrue(actual.getFormatFamily() instanceof UnsupportedFormatFamily);
    }

    @Test
    public void should_not_detect_a_png() throws Exception {

        String fileName = "talend_product_icon_dataprep.png";

        Format actual = formatDetector.detect(this.getClass().getResourceAsStream(fileName));
        assertTrue(actual.getFormatFamily() instanceof UnsupportedFormatFamily);
    }

}