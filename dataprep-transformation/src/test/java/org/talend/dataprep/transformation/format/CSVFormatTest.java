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