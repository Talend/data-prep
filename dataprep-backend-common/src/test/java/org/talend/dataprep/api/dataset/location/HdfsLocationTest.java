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

package org.talend.dataprep.api.dataset.location;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.URIException;
import org.junit.Test;

/**
 * Unit test for the HdfsLocation
 * 
 * @see HdfsLocation
 */
public class HdfsLocationTest {

    @Test
    public void typeShouldBeHDFS() throws URIException {
        HdfsLocation location = new HdfsLocation();
        assertEquals("hdfs", location.getLocationType());
    }

    @Test
    public void shouldEncodeUrl() throws URIException {
        HdfsLocation location = new HdfsLocation();
        location.setUrl("hdfs://localhost/test/url with spaces.csv");
        assertEquals("hdfs://localhost/test/url%20with%20spaces.csv", location.getUrl());
    }

    @Test
    public void shouldNotEncodeUrl() throws URIException {
        HdfsLocation location = new HdfsLocation();
        location.setUrl("  ");
        assertEquals("  ", location.getUrl());
    }
}