// ============================================================================
//
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

package org.talend.dataprep.schema.csv;

import static org.junit.Assert.assertEquals;
import static org.talend.dataprep.schema.csv.CSVFormatFamily.HEADER_NB_LINES_PARAMETER;
import static org.talend.dataprep.schema.csv.CSVFormatFamily.SEPARATOR_PARAMETER;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for CSVFormatUtils.
 * 
 * @see CSVFormatUtils
 */
@RunWith(MockitoJUnitRunner.class)
public class CSVFormatUtilsTest {

    /** The component to test. */
    @InjectMocks
    private CSVFormatUtils csvFormatUtils = new CSVFormatUtils();

    @Mock
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldUseNewSeparator() {

        // when
        final Map<String, String> entryParameters = new HashMap<>();
        entryParameters.put(HEADER_NB_LINES_PARAMETER, "12");

        final Separator separator = new Separator("|".charAt(0));

        // when
        csvFormatUtils.compileParameterProperties(separator, entryParameters);

        // then
        assertEquals("|", entryParameters.get(SEPARATOR_PARAMETER));
    }

}