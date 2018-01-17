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

package org.talend.dataprep.schema.html;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.html.HtmlEncodingDetector;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.Format;

public class HtmlDetectorTest extends AbstractSchemaTestUtils {

    @Autowired
    private HtmlDetector htmlDetector;

    @Test
    public void guess_html_format_success() throws Exception {

        String fileName = "sales-force.xls";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Charset charset = new HtmlEncodingDetector().detect(this.getClass().getResourceAsStream(fileName), new Metadata());
        Format actual = htmlDetector.detect(this.getClass().getResourceAsStream(fileName));

        assertTrue(actual.getFormatFamily() instanceof HtmlFormatFamily);
        assertTrue(StringUtils.equals("UTF-16", actual.getEncoding()));
    }

    @Test
    public void guess_html_format_fail() throws Exception {

        String fileName = "foo.html";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Format actual = htmlDetector.detect(this.getClass().getResourceAsStream(fileName));
        assertNull(actual);
    }

}