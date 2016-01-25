package org.talend.dataprep.transformation.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Test;

/**
 * Test the CSV format.
 */
public class CSVFormatTest extends BaseFormatTest {

    @Test
    public void csv() throws IOException {
        testFormat(new CSVFormat(), "csv.json");
    }

    @Test
    public void testOrder() throws Exception {
        assertThat(new CSVFormat().getOrder(), is(0));
    }
}