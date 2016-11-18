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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.action.ActionDefinition;

/**
 * TODO: Add a test to check error dataprep error messages and a lookup with two more actions.
 */
public class DefaultActionParserTest {

    private static ServerMock serverMock;

    private static String apiUrl;

    private static final String login = "login";

    private static final String password = "password";

    private final Header header = new BasicHeader("Authorization", "Bearer ABC");

    private final String preparationId = "A1B2C3";

    private final String dataSetId = "A1B2C3";

    private static ActionParser parser;

    @BeforeClass
    public static void init() throws Exception {
        serverMock = new ServerMock();
        apiUrl = serverMock.getServerUrl();
        parser = new DefaultActionParser(apiUrl, login, password);
    }

    @After
    public void tearDown() throws Exception {
        serverMock.removeAllEndPoints();
    }

    @Test
    public void testEmptyActions() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "[]", header);
        serverMock.addEndPoint("/login", "", header);
        // When
        final Function<IndexedRecord, IndexedRecord> emptyArray = parser.parse(preparationId);
        // Then
        assertNotNull(emptyArray);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidJSON() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "{", header);
        serverMock.addEndPoint("/login", "", header);
        // When
        parser.parse(preparationId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyJSON() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "", header);
        serverMock.addEndPoint("/login", "", header);
        parser.parse(preparationId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "", header);
        serverMock.addEndPoint("/login", "", header);
        parser.parse(preparationId);
    }

    @Test
    public void testActionSample() throws Exception {
        // Given
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("actions_sample1.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }

        // Then
        assertNotNull(function);
    }

    @Test
    public void testUpperCaseAction() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_uppercase.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("string", record.get(0));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("STRING", result.get(0));
    }

    @Test
    public void testSplitCaseAction() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string string", "value" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_split.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("string string", record.get(0));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("string string", result.get(0));
        assertEquals("string", result.get(1));
        assertEquals("string", result.get(2));
        assertEquals("", result.get(3));
    }

    @Test
    public void testSplitCaseActionValueOrder() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string string", "value" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_split.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("string string", record.get(0));
        assertEquals("value", record.get(1));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("string string", result.get(0));
        assertEquals("string", result.get(1));
        assertEquals("string", result.get(2));
        assertEquals("", result.get(3));
        assertEquals("value", result.get(4));
    }

    @Test
    public void testSplitWithFilter() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "CA", "value to split" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "NY", "value NOT to split" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_split_filter.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);

        // Then
        assertEquals("CA", result1.get(0));
        assertEquals("value to split", result1.get(1));
        assertEquals("value", result1.get(2));
        assertEquals("to split", result1.get(3));
        assertEquals("NY", result2.get(0));
        assertEquals("value NOT to split", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
    }

    @Test
    public void testDeleteAction() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_delete.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("", record.get(0));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertNull(result);
    }

    @Test
    public void testDeleteInvalidAction() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "1" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "1" });
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] { "A" });

        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class
                .getResourceAsStream("action_delete_invalid.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNull(result3);
    }

    @Test
    public void testNumericOperation() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "1", "2" });

        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_numeric_ops.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("1", result.get(0));
        assertEquals("3", result.get(1));
        assertEquals("2", result.get(2));
    }

    @Test
    public void shouldAllBeSerializable() throws Exception {
        ClassPathActionRegistry registry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");
        final List<? extends Class<? extends ActionDefinition>> nonSerializableActions = registry.getAll().map(action -> { //
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new NullOutputStream());
                oos.writeObject(action.newInstance());
                oos.flush();
                return null;
            } catch (Exception e) {
                return action;
            }
        }).filter(c -> c != null).collect(Collectors.toList());

        assertTrue("Non serializable actions : " + Arrays.toString(nonSerializableActions.toArray()),
                nonSerializableActions.isEmpty());
    }

    @Test
    public void testNoAction() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "[]", header);
        serverMock.addEndPoint("/login", "", header);
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        final Function<IndexedRecord, IndexedRecord> function = parser.parse(preparationId);
        assertNotNull(function);
        assertEquals("string", record.get(0));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("string", result.get(0));
    }

    @Test
    public void testDeleteColumnAction() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string", "string_to_delete" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class
                .getResourceAsStream("action_delete_column.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("string", record.get(0));
        assertEquals("string_to_delete", record.get(1));
        assertEquals(2, record.getSchema().getFields().size());

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertEquals("string", result.get(0));
        assertEquals(1, result.getSchema().getFields().size());
    }

    @Test
    public void testSingleLookupAction() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream preparationStream = DefaultActionParserTest.class.getResourceAsStream("single_lookup.json");
                InputStream dataSetStream = DefaultActionParserTest.class.getResourceAsStream("dataset.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", preparationStream, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId+"*", dataSetStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("Rose Bowl", record1.get(0));
        assertEquals("CA", record1.get(1));
        assertEquals(2, record1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", record2.get(0));
        assertEquals("SN", record2.get(1));
        assertEquals(2, record2.getSchema().getFields().size());

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);

        // Then
        assertEquals("Rose Bowl", result1.get(0));
        assertEquals("CA", result1.get(1));
        assertEquals("California", result1.get(2));
        assertEquals("Sacramento", result1.get(3));
        assertEquals(4, result1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", result2.get(0));
        assertEquals("SN", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
        assertEquals(4, result2.getSchema().getFields().size());
    }

    @Test
    public void testUpperWithLookupAction() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream preparationStream = DefaultActionParserTest.class.getResourceAsStream("upper_with_lookup.json");
                InputStream dataSetStream = DefaultActionParserTest.class.getResourceAsStream("dataset.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", preparationStream, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId+"*", dataSetStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);
        assertEquals("Rose Bowl", record1.get(0));
        assertEquals("CA", record1.get(1));
        assertEquals(2, record1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", record2.get(0));
        assertEquals("SN", record2.get(1));
        assertEquals(2, record2.getSchema().getFields().size());

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);

        // Then
        assertEquals("Rose Bowl", result1.get(0));
        assertEquals("CA", result1.get(1));
        assertEquals("CALIFORNIA", result1.get(2));
        assertEquals("Sacramento", result1.get(3));
        assertEquals(4, result1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", result2.get(0));
        assertEquals("SN", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
        assertEquals(4, result2.getSchema().getFields().size());
    }
}