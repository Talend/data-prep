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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.actions.resources.DictionaryResource;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataquality.semantic.broadcast.TdqCategoriesFactory;

public class StandalonePreparationFactoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandalonePreparationFactoryTest.class);

    private StandalonePreparationFactory factory = new StandalonePreparationFactory();

    private static DictionaryResource dictionaryResource = new DictionaryResource(TdqCategoriesFactory.createFullTdqCategories());

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidJSON() throws Exception {
        factory.create(IOUtils.toInputStream("{", "UTF-8"), dictionaryResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyJSON() throws Exception {
        factory.create(IOUtils.toInputStream("", "UTF-8"), dictionaryResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() throws Exception {
        factory.create((InputStream) null, dictionaryResource);
    }

    @Test
    public void testActionSample() throws Exception {
        // Given
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("actions_sample1.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
    public void testSplitCaseAction() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string string", "value" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("action_split.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
        try (final InputStream resourceAsStream = DefaultActionParser.class
                .getResourceAsStream("action_delete_invalid_email.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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

        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "string" });
        final Function<IndexedRecord, IndexedRecord> function = factory.create(IOUtils.toInputStream("{\"actions\":[]}", "UTF-8"),
                dictionaryResource);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
    public void testDeleteInvalidCountries() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "1", "01/01/1970", "Taboulistan" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "1", "5/24/1982", "France" });
        final IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] { "1", "11/9/1970", "United States" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class
                .getResourceAsStream("delete_invalid_country_preparation.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();

            // When
            function = recipeFunctionFactory.create(dataSetStream);
        }

        // Then
        assertNotNull(function);
        assertEquals("Taboulistan", record1.get(2));
        assertEquals("France", record2.get(2));
        assertEquals("United States", record3.get(2));

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertNull(result1);

        assertEquals("1", result2.get(0));
        assertEquals("5/24/1982", result2.get(1));
        assertEquals("France", result2.get(2));
        assertEquals(3, result2.getSchema().getFields().size());

        assertEquals("1", result3.get(0));
        assertEquals("11/9/1970", result3.get(1));
        assertEquals("United States", result3.get(2));
        assertEquals(3, result3.getSchema().getFields().size());
    }

    @Test
    public void testSplitUpperDeletePreparation() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper
                .createRecord(new Object[] { "1", "01/01/1970", "Taboulistan", "6.0", "22.0", "F", "False", "162" });
        final IndexedRecord record2 = GenericDataRecordHelper
                .createRecord(new Object[] { "1", "5/24/1982", "France", "4.0", "44.0", "F", "False", "193" });
        final IndexedRecord record3 = GenericDataRecordHelper
                .createRecord(new Object[] { "1", "11/9/1970", "United States", "7.0", "10.0", "M", "True", "134" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class
                .getResourceAsStream("split_upper_delete_preparation.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();
            function = recipeFunctionFactory.create(dataSetStream);
        }
        assertNotNull(function);
        assertEquals("Taboulistan", record1.get(2));
        assertEquals("France", record2.get(2));
        assertEquals("United States", record3.get(2));

        // When
        final IndexedRecord result1 = function.apply(record1);
        final IndexedRecord result2 = function.apply(record2);
        final IndexedRecord result3 = function.apply(record3);

        // Then
        assertEquals("TABOULISTAN", result1.get(2));
        assertEquals("FRANCE", result2.get(2));
        assertEquals("UNITED STATES", result3.get(2));

        assertEquals("TABOULISTAN", result1.get(3));
        assertEquals("FRANCE", result2.get(3));
        assertEquals("UNITED", result3.get(3));

        assertEquals("", result1.get(4));
        assertEquals("", result2.get(4));
        assertEquals("STATES", result3.get(4));

        assertEquals(9, result1.getSchema().getFields().size());
        assertEquals(9, result2.getSchema().getFields().size());
        assertEquals(9, result3.getSchema().getFields().size());
    }

    @Test
    public void testSingleLookupAction() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class
                .getResourceAsStream("standalone_preparation_one_lookup_one_upper.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();
            function = recipeFunctionFactory.create(dataSetStream);
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
    public void testUpperWithSingleLookupAction() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class
                .getResourceAsStream("standalone_preparation_single_lookup.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();
            function = recipeFunctionFactory.create(dataSetStream);
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
    public void testTwoLookupActionsWithUppercase() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class
                .getResourceAsStream("standalone_preparation_two_lookups_2_upper.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();
            function = recipeFunctionFactory.create(dataSetStream);
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
            function = factory.create(resourceAsStream, dictionaryResource);
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
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { initialEmailAddress });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_mask_email.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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

    @Test
    public void testDuplicateColumnName() throws Exception {
        // Given
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "My String" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_duplicate_column.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
        }
        assertNotNull(function);

        // Then
        assertSerializable(function);
        final IndexedRecord apply = function.apply(record);// Preparation tries to create a new column with a name that previously
                                                           // existed, but ok.
        assertEquals(2, apply.getSchema().getFields().size());
        assertNotNull(apply.getSchema().getField("a1"));
        assertNotNull(apply.getSchema().getField("a1_1"));
    }

    @Test
    public void testStringClustering() throws Exception {
        // Given
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "cluster1" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "cluster2" });
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] { "unique" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_text_clustering.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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
        IndexedRecord record = GenericDataRecordHelper.createRecord(new Object[] { "email@email.com" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class.getResourceAsStream("action_split_email.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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
        IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "1" });
        IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "a" });
        IndexedRecord record3 = GenericDataRecordHelper.createRecord(new Object[] { "" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream resourceAsStream = DefaultActionParser.class
                .getResourceAsStream("action_invalid_empty_keep.json")) {
            function = factory.create(resourceAsStream, dictionaryResource);
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

    @Test
    public void TDP_3518() throws Exception {
        // Given
        final IndexedRecord record1 = GenericDataRecordHelper.createRecord(new Object[] { "Rose Bowl", "CA" });
        final IndexedRecord record2 = GenericDataRecordHelper.createRecord(new Object[] { "Beaver Stadium", "SN" });
        final Function<IndexedRecord, IndexedRecord> function;
        try (final InputStream dataSetStream = DefaultActionParserTest.class.getResourceAsStream("empty_filter.json")) {
            StandalonePreparationFactory recipeFunctionFactory = new StandalonePreparationFactory();
            function = recipeFunctionFactory.create(dataSetStream);
        }
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
