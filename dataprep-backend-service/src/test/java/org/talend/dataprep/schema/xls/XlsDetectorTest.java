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

package org.talend.dataprep.schema.xls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.Format;

public class XlsDetectorTest extends AbstractSchemaTestUtils {

    /** The format guesser to test. */
    @Autowired
    XlsDetector xlsDetector;

    /**
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void should_not_read_null_input_stream() throws Exception {
        xlsDetector.detect(null);
    }

    @Test
    public void should_detect_old_xls_format() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("test.xls")) {
            Format actual = xlsDetector.detect(inputStream);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }
    }

    @Test
    public void should_detect_new_xls_format() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("test_new.xlsx")) {
            Format actual = xlsDetector.detect(inputStream);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }
    }

    @Test
    public void read_xls_that_can_be_parsed_as_csv_TDP_375() throws Exception {

        String fileName = "TDP-375_xsl_read_as_csv.xls";

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            Format actual = xlsDetector.detect(inputStream);
            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatFamily() instanceof XlsFormatFamily);
            assertEquals(XlsFormatFamily.MEDIA_TYPE, actual.getFormatFamily().getMediaType());
            assertTrue(StringUtils.equals("UTF-8", actual.getEncoding()));
        }

    }

}