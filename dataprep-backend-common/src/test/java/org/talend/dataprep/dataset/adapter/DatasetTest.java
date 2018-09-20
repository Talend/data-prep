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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
public class DatasetTest {

    @Test
    public void JsonDeserialization() throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("dataset-catalog-sample.json");

        Dataset dataset = new ObjectMapper() //
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .readValue(resourceAsStream, Dataset.class);

        assertNotNull(dataset);
        assertNotNull(dataset.getId());
        assertThat(dataset.getCertification(), is(Dataset.CertificationState.NONE));
    }
}
