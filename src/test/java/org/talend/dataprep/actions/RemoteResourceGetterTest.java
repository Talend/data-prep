// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataquality.semantic.broadcast.TdqCategories;
import org.talend.dataquality.semantic.broadcast.TdqCategoriesFactory;

public class RemoteResourceGetterTest {

    private static ServerMock serverMock;

    private final Header header = new BasicHeader("Authorization", "Bearer ABC");

    private final String goodDataSetId = "A1B2C3";

    private final String badDataSetId = "A1B2C34";

    private RemoteResourceGetter remoteResourceGetter;

    @BeforeClass
    public static void init() throws Exception {
        serverMock = new ServerMock();
    }

    @After
    public void tearDown() throws Exception {
        serverMock.removeAllEndPoints();
    }

    @Test
    public void testLoginOK() throws Exception {
        // Given
        serverMock.addEndPoint("/login", "", header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        Header result = remoteResourceGetter.login(serverUrl, "Maximus", "Spanish");

        // Then
        assertEquals(header.getName(), result.getName());
        assertEquals(header.getValue(), result.getValue());
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testLoginKO() throws Exception {
        // Given
        serverMock.addEndPoint("/login", "", header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // then
        remoteResourceGetter.login(serverUrl + "/log", "Maximus", "Spanish");
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testGetErrorCode() throws Exception {
        // Given
        try(InputStream inputStream = RemoteResourceGetterTest.class.getResourceAsStream("error_code.json")) {
            serverMock.addEndPoint("/errorCode", inputStream, 404, header);
        }
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        Header result = remoteResourceGetter.login(serverUrl + "/errorCode", "Maximus", "Spanish");

        // Then
        fail();
    }

    @Test
    public void testGetDataSet() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/*", RemoteResourceGetterTest.class.getResourceAsStream("lookup_dataset1.json"), header);
        serverMock.addEndPoint("/login", "", header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        LightweightExportableDataSet result = remoteResourceGetter.retrieveLookupDataSet(serverUrl, "Maximus", "Spanish",
                goodDataSetId, "0000");

        // Then
        assertEquals(5, result.size());
        assertEquals("California", result.getRecords().get("CA").get("0001"));
        assertEquals("Sacramento", result.getRecords().get("CA").get("0002"));
        assertEquals("Alabama", result.getRecords().get("AL").get("0001"));
        assertEquals("Montgomery", result.getRecords().get("AL").get("0002"));
    }

    /**
     * Created after bug https://jira.talendforge.org/browse/TDP-3098
     */
    @Test
    public void testGetDataSet_withTcompLookup_TDP3098() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/*", this.getClass().getResourceAsStream("lookup_dataset_from_Tcomp.json"), header);
        serverMock.addEndPoint("/login", "", header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // When
        LightweightExportableDataSet result = remoteResourceGetter.retrieveLookupDataSet(serverUrl, "Maximus", "Spanish",
                goodDataSetId, "0000");

        // Then
        assertEquals(1001, result.size());
        Map<String, String> firstRecord = result.getRecords().get("1");
        assertEquals("Helena", firstRecord.get("0001"));
        assertEquals("Austin", firstRecord.get("0002"));
        Map<String, String> twentyThirdRecord = result.getRecords().get("23");
        assertEquals("mwelchm@virginia.edu", twentyThirdRecord.get("0003"));
        assertEquals("247.82.30.113", twentyThirdRecord.get("0005"));
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testGetDataSetNoRecords() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/", "{ \"metadata\": { \"columns\": [ { \"id\": \"0000\", \"name\": \"Postal\"}]}}",
                header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // then
        remoteResourceGetter.retrieveLookupDataSet(serverUrl, "Maximus", "Spanish", badDataSetId, "0000");
    }

    @Test(expected = RemoteResourceGetter.RemoteConnectionException.class)
    public void testGetDataSetNoMetadata() throws Exception {
        // Given
        serverMock.addEndPoint("/api/datasets/", "{ \"object\": \"\"}", header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // then
        remoteResourceGetter.retrieveLookupDataSet(serverUrl, "Maximus", "Spanish", badDataSetId, "0000");
    }

    @Test
    public void testGetDictionaries() throws Exception {
        // Given
        TdqCategories o = TdqCategoriesFactory.createEmptyTdqCategories();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos))) {
            oos.writeObject(o);
        }
        serverMock.addEndPoint("/login", "", header);
        serverMock.addEndPoint("/api/transform/dictionary", new ByteArrayInputStream(bos.toByteArray()), header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();
        // When
        TdqCategories result = remoteResourceGetter.retrieveDictionaries(serverUrl, "Maximus", "Spanish");

        // Then
        assertNotNull(result);
        assertTrue(result.getDictionary().getDocumentList().isEmpty());
        assertTrue(result.getKeyword().getDocumentList().isEmpty());
    }

    @Test
    public void shouldGetPreparation() throws Exception {
        // given
        serverMock.addEndPoint("/login", "", header);
        String preparationId = "f52382cf-2e11-4c42-a2ec-0dc84f1dfa2f";
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details",
                this.getClass().getResourceAsStream("preparation.json"), header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // when
        final String actual = remoteResourceGetter.retrievePreparation(serverUrl, "Maximus", "Spanish", preparationId, null);

        // then
        assertNotNull(actual);
        assertEquals(IOUtils.toString(this.getClass().getResourceAsStream("preparation.json"), UTF_8), actual);
    }

    @Test
    public void shouldGetPreparationWithVersion() throws Exception {
        // given
        serverMock.addEndPoint("/login", "", header);
        String preparationId = "f52382cf-2e11-4c42-a2ec-0dc84f1dfa2f";
        String versionId = "123-ABC-456-DEF";
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/versions/" + versionId + "/details",
                this.getClass().getResourceAsStream("preparation.json"), header);
        String serverUrl = serverMock.getServerUrl();
        remoteResourceGetter = new RemoteResourceGetter();

        // when
        final String actual = remoteResourceGetter.retrievePreparation(serverUrl, "Maximus", "Spanish", preparationId, versionId);

        // then
        assertNotNull(actual);
        assertEquals(IOUtils.toString(this.getClass().getResourceAsStream("preparation.json"), UTF_8), actual);
    }
}
