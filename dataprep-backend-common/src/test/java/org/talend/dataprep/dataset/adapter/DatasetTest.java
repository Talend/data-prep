/*
 * ============================================================================
 *
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

@Ignore
public class DatasetTest {

    @Test
    public void JsonDeserialization() throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("dataset-catalog-sample.json");

        Dataset dataset = new ObjectMapper().readValue(resourceAsStream, Dataset.class);

        assertNotNull(dataset);
        assertNotNull(dataset.getId());
    }
}
