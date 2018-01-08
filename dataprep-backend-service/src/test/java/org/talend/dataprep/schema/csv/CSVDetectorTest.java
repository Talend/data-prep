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

package org.talend.dataprep.schema.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.Format;

public class CSVDetectorTest extends AbstractSchemaTestUtils {

    @Autowired
    private CSVDetector csvDetector;

    /**
     * Standard csv file.
     */
    @Test
    public void should_detect_CSV_format_and_encoding() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("standard.csv")) {
            Format actual = csvDetector.detect(inputStream);

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatFamily() instanceof CSVFormatFamily);
            assertEquals("ISO-8859-1", actual.getEncoding());

        }
    }

    /**
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void should_not_read_null_input_stream() throws Exception {
        csvDetector.detect(null);
    }

}