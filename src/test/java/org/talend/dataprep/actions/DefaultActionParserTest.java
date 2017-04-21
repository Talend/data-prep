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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.transformation.service.Dictionaries;

public class DefaultActionParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionParserTest.class);

    private static final String login = "login";

    private static final String password = "password";

    private static final Header header = new BasicHeader("Authorization", "Bearer ABC");

    private static final String preparationId = "A1B2C3";

    private static final String versionId = "1A2B3C";

    private static final String dataSetId1 = "A1B2C3";

    private static final String dataSetId2 = "A1B2C3D4";

    private static ServerMock serverMock;

    private static ActionParser parser;

    private static String apiUrl;

    private static byte[] sampleDictionary;

    @BeforeClass
    public static void init() throws Exception {
        serverMock = new ServerMock();
        apiUrl = serverMock.getServerUrl();
        parser = new DefaultActionParser(apiUrl, login, password);

        Dictionaries o = new Dictionaries(null, null);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos))) {
            oos.writeObject(o);
        }
        sampleDictionary = bos.toByteArray();
    }

    @After
    public void tearDown() throws Exception {
        serverMock.removeAllEndPoints();
    }

    @Test
    public void testEmptyActions() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "{\"actions\":[]}", header);
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
        assertSerializable(function);
        assertNotNull(function);
    }

    @Test
    public void testPreparationWithVersion() throws Exception {
        // Given
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("actions_sample1.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/versions/" + versionId + "/details", resourceAsStream,
                    header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId, versionId);
        }

        // Then
        assertSerializable(function);
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
        assertSerializable(function);
        assertEquals("STRING", result.get(0));
    }

    @Test
    public void testUpperCaseActionWithFilterOut() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "filtered out" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_uppercase_filter_out.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);

        // Then
        assertSerializable(function);
        assertNull(result1);
        assertNotNull(result2);
        assertEquals("STRING", result2.get(0));
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
        assertSerializable(function);
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
        assertSerializable(function);
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
        assertSerializable(function);
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
        assertSerializable(function);
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
            serverMock.addEndPoint("/api/transform/dictionary", new ByteArrayInputStream(sampleDictionary), header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertSerializable(function);
        assertNotNull(result1);
        assertNotNull(result2);
        assertNull(result3);
    }

    @Test
    public void testDeleteInvalidEmailAction() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "test@test.org" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "A" });
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] { "email@server.com" });

        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_delete_invalid_email.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            serverMock.addEndPoint("/api/transform/dictionary", new ByteArrayInputStream(sampleDictionary), header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertNotNull(result1);
        assertNull(result2);
        assertNotNull(result3);
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
        assertSerializable(function);
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
        }).filter(Objects::nonNull).collect(Collectors.toList());

        assertTrue("Non serializable actions : " + Arrays.toString(nonSerializableActions.toArray()),
                nonSerializableActions.isEmpty());
    }

    @Test
    public void testNoAction() throws Exception {
        // Given
        serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", "{\"actions\":[]}", header);
        serverMock.addEndPoint("/login", "", header);
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        final Function<IndexedRecord, IndexedRecord> function = parser.parse(preparationId);
        assertNotNull(function);
        assertEquals("string", record.get(0));

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertSerializable(function);
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
        assertSerializable(function);
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
                final InputStream dataSetStream = DefaultActionParserTest.class.getResourceAsStream("lookup_dataset1.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", preparationStream, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId1 +"*", dataSetStream, header);
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
        assertEquals("Sacramento", result1.get(2));
        assertEquals("California", result1.get(3));
        assertEquals(4, result1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", result2.get(0));
        assertEquals("SN", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
        assertEquals(4, result2.getSchema().getFields().size());
    }

    @Test
    public void testUpperWithSingleLookupAction() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream preparationStream = DefaultActionParserTest.class.getResourceAsStream("upper_with_lookup.json");
                final InputStream dataSetStream = DefaultActionParserTest.class.getResourceAsStream("lookup_dataset1.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", preparationStream, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId1 +"*", dataSetStream, header);
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
        assertEquals("SACRAMENTO", result1.get(2));
        assertEquals("California", result1.get(3));
        assertEquals(4, result1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", result2.get(0));
        assertEquals("SN", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
        assertEquals(4, result2.getSchema().getFields().size());
    }

    @Test
    public void testTwoLookupActionsWithUppercase() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream preparationStream = DefaultActionParserTest.class.getResourceAsStream("two_lookups_with_upper.json");
                final InputStream dataSetStream1 = DefaultActionParserTest.class.getResourceAsStream("lookup_dataset1.json");
                final InputStream dataSetStream2 = DefaultActionParserTest.class.getResourceAsStream("lookup_dataset2.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", preparationStream, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId1 +"*", dataSetStream1, header);
            serverMock.addEndPoint("/api/datasets/" + dataSetId2 +"*", dataSetStream2, header);
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
        assertEquals("WEST", result1.get(2));
        assertEquals("SACRAMENTO", result1.get(3));
        assertEquals("California", result1.get(4));
        assertEquals(5, result1.getSchema().getFields().size());

        assertEquals("Beaver Stadium", result2.get(0));
        assertEquals("SN", result2.get(1));
        assertEquals("", result2.get(2));
        assertEquals("", result2.get(3));
        assertEquals("", result2.get(4));
        assertEquals(5, result2.getSchema().getFields().size());
    }

    @Test
    public void testMakeLineAsHeaderAction() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "header" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_make_line_header.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);

        // Then -> Make as Line as Header
        assertSerializable(function);
        assertEquals("string", result1.get(0));
        assertEquals("header", result2.get(0));
        assertEquals(1, result1.getSchema().getFields().size());
    }

    @Test
    public void testDataMaskingAction() throws Exception {
        // Given
        final String initialEmailAddress = "email@email.com";
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] {initialEmailAddress});
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_mask_email.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            serverMock.addEndPoint("/api/transform/dictionary", new ByteArrayInputStream(sampleDictionary), header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // When
        final IndexedRecord result = function.apply(record);

        // Then
        assertSerializable(function);
        assertNotEquals(initialEmailAddress, result.get(0));
        assertEquals(initialEmailAddress.length(), String.valueOf(result.get(0)).length());
        assertTrue(String.valueOf(result.get(0)).contains("@"));
    }

    @Test(expected = AvroRuntimeException.class)
    public void testDuplicateColumnName() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] {"My String"});
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_duplicate_column.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // Then
        assertSerializable(function);
        function.apply(record); // Preparation tries to create a new column with a name that previously existed.
    }

    @Test
    public void testStringClustering() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] {"cluster1"});
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] {"cluster2"});
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] {"unique"});
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_text_clustering.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // Given
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertSerializable(function);
        assertEquals("cluster", result1.get(0));
        assertEquals("cluster", result2.get(0));
        assertEquals("unique", result3.get(0));
    }

    @Test
    public void testEmailSplit() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] {"email@email.com"});
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_split_email.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // Given
        final IndexedRecord result = function.apply(record);

        // Then
        assertSerializable(function);
        assertEquals("email@email.com", result.get(0));
        assertEquals("email", result.get(1));
        assertEquals("email.com", result.get(2));
        assertEquals("a1_local", result.getSchema().getFields().get(1).name());
        assertEquals("a1_domain", result.getSchema().getFields().get(2).name());
    }

    @Test
    public void testKeepInvalidAndEmpty() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] {"1"});
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] {"a"});
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] {""});
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_invalid_empty_keep.json")) {
            serverMock.addEndPoint("/api/preparations/" + preparationId + "/details", resourceAsStream, header);
            serverMock.addEndPoint("/login", "", header);
            function = parser.parse(preparationId);
        }
        assertNotNull(function);

        // Given
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertSerializable(function);
        assertNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
    }

    private static void assertSerializable(Function<IndexedRecord, IndexedRecord> function) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new NullOutputStream());
            oos.writeObject(function);
            oos.flush();
        } catch (Exception e) {
            LOGGER.error("Unable to serialize function.", e);
            fail();
        }
    }

}
