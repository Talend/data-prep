// ============================================================================
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

package org.talend.dataprep.transformation.format;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Test the CSV format.
 */
public class JsonFormatTest extends BaseFormatTest {

    private JsonFormat format;

    @Before
    public void setUp() {
        super.setUp();
        format = new JsonFormat();
    }

    @Test
    public void json() throws IOException {
        testFormat(format, "json.json");
    }

    @Test
    public void testOrder() throws Exception {
        assertThat(format.getOrder(), is(-1));
    }

    @Test
    public void shouldBeCompatibleWithAll() throws Exception {
        assertTrue(format.isCompatible(null));
        assertTrue(format.isCompatible(new DataSetMetadata()));
    }

    @Test
    public void shouldSupportSampling() throws Exception {
        assertTrue(format.supportSampling());
    }
}
