// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static junit.framework.TestCase.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.parameters.Parameter;

/**
 * Test the CSV format.
 */
public class CSVFormatTest extends BaseFormatTest {

    private CSVFormat format;

    @Before
    public void setUp() {
        super.setUp();
        format = new CSVFormat();
    }

    @Test
    public void csv() throws IOException {
        // when
        final ExportFormatMessage exportFormatMessage = beanConversionService.convert(format, ExportFormatMessage.class);

        // then
        assertEquals("text/csv", exportFormatMessage.getMimeType());
        assertEquals("CSV", exportFormatMessage.getId());
        assertEquals("Local CSV file", exportFormatMessage.getName());
        assertEquals(true, exportFormatMessage.isNeedParameters());
        assertEquals(false, exportFormatMessage.isDefaultExport());
        assertEquals(true, exportFormatMessage.isEnabled());
        assertEquals("", exportFormatMessage.getDisableReason());
        assertEquals("Export to CSV", exportFormatMessage.getTitle());
        List<Parameter> parameters = exportFormatMessage.getParameters();
        assertNotNull(parameters);
        assertEquals(6, parameters.size());
    }

    @Test
    public void testOrder() throws Exception {
        assertThat(format.getOrder(), is(0));
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
