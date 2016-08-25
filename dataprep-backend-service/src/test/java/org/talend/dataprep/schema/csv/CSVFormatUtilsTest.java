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

package org.talend.dataprep.schema.csv;

import static org.junit.Assert.assertEquals;
import static org.talend.dataprep.schema.csv.CSVFormatFamily.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;

/**
 * Unit test for CSVFormatUtils.
 * 
 * @see CSVFormatUtils
 */

public class CSVFormatUtilsTest extends AbstractSchemaTestUtils {

    /** The component to test. */
    @Autowired
    private CSVFormatUtils csvFormatUtils;

    @Test
    public void shouldUseNewSeparator() {

        final DataSetMetadata updated = metadataBuilder.metadata() //
                .id("updated") //
                .parameter(HEADER_COLUMNS_PARAMETER,
                        "[\"nickname|secret\",\"firstname|secret\",\"lastname|date\",\"of\",\"birth|city\"]")
                .parameter(SEPARATOR_PARAMETER, "|") // new separator
                .parameter(HEADER_NB_LINES_PARAMETER, "12").build();

        // when
        csvFormatUtils.useNewSeparator(updated);

        // then
        final Map<String, String> parameters = updated.getContent().getParameters();
        assertEquals("[]", parameters.get(HEADER_COLUMNS_PARAMETER));
        assertEquals("|", parameters.get(SEPARATOR_PARAMETER));
        assertEquals("12", parameters.get(HEADER_NB_LINES_PARAMETER));
    }
}