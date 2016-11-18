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

package org.talend.dataprep.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class RemoteResourceGetterTest {

    private final Header header = new BasicHeader("Authorization", "Bearer ABC");

    private final String goodDataSetId = "A1B2C3";

    private final String badDataSetId = "A1B2C34";

    private RemoteResourceGetter remoteResourceGetter;

    private static ServerMock serverMock;

    @BeforeClass
    public static void init() throws Exception{
        serverMock = new ServerMock();
    }

    @After
    public void tearDown()throws Exception{
        serverMock.removeAllEndPoints();
    }

    @Test
    public void testLoginOK() throws Exception {
        // Given
        serverMock.addEndPoint("/login", "" ,header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        Header result = remoteResourceGetter.login(serverUrl , "Maximus", "Spanish");

        // Then
        assertEquals(header.getName(), result.getName());
        assertEquals(header.getValue(), result.getValue());
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testLoginKO() throws Exception {
        // Given
        serverMock.addEndPoint("/login", "" ,header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        Header result = remoteResourceGetter.login(serverUrl + "/log", "Maximus", "Spanish");

        // Then
        fail();
    }

    @Test
    public void testGetDataSet() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/*", RemoteResourceGetterTest.class.getResourceAsStream("dataset.json"),header);
        serverMock.addEndPoint("/login", "",header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();
        // When
        Map<String, DataSetRow> result = remoteResourceGetter.retrieveLookupDataSet(serverUrl,"Maximus", "Spanish", goodDataSetId, "0000");

        // Then
        assertEquals(2, result.size());
        assertEquals("California", result.get("CA").values().get("0001"));
        assertEquals("Sacramento", result.get("CA").values().get("0002"));
        assertEquals("Alabama", result.get("AL").values().get("0001"));
        assertEquals("Montgomery", result.get("AL").values().get("0002"));
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testGetDataSetNoRecords() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/", "{ \"metadata\": { \"columns\": [ { \"id\": \"0000\", \"name\": \"Postal\"}]}}",header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();
        // When
        remoteResourceGetter.retrieveLookupDataSet(serverUrl,"Maximus", "Spanish", badDataSetId, "0000");

        // Then
        fail();
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testGetDataSetNoMetadata() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/", "{ \"object\": \"\"}",header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();
        // When
        remoteResourceGetter.retrieveLookupDataSet(serverUrl,"Maximus", "Spanish", badDataSetId, "0000");

        // Then
        fail();
    }

}