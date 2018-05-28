/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter.commands;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.security.Security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.dataset.adapter.MockDatasetServer.AUTHENTICATION_TOKEN;

public class DataSetGetSchemaTest extends ServiceBaseTest {

    @Autowired
    ApplicationContext context;

    @MockBean
    Security security;

    @Test
    public void name() {
        when(security.getAuthenticationToken()).thenReturn(AUTHENTICATION_TOKEN);

        DataSetGetMetadata command = context.getBean(DataSetGetMetadata.class, "no-matter");

        Dataset dataset = command.execute();

        assertEquals(HttpStatus.OK, command.getStatus());
        assertNotNull(dataset);
        // check dataset does not contains null values
        assertNotNull(dataset.getId());
        assertNotNull(dataset.getLabel());
    }
}
