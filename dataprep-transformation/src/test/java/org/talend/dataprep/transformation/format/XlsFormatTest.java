package org.talend.dataprep.transformation.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Test;

/**
 * Test the CSV format.
 */
public class XlsFormatTest extends BaseFormatTest {

    @Test
    public void xls() throws IOException {
        testFormat(new XlsFormat(), "xls.json");
    }

    @Test
    public void testOrder() throws Exception {
        assertThat(new XlsFormat().getOrder(), is(1));
    }
}