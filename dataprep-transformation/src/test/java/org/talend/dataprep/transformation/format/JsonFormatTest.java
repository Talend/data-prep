package org.talend.dataprep.transformation.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Test;

/**
 * Test the CSV format.
 */
public class JsonFormatTest extends BaseFormatTest {

    @Test
    public void json() throws IOException {
        testFormat(new JsonFormat(), "json.json");
    }

    @Test
    public void testOrder() throws Exception {
        assertThat(new JsonFormat().getOrder(), is(-1));
    }
}