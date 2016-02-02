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