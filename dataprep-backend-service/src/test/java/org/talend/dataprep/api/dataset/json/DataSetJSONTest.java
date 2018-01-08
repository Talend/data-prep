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

package org.talend.dataprep.api.dataset.json;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.csv.CSVFormatFamily;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSetJSONTest extends ServiceBaseTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /**
     * @param json A valid JSON stream, may be <code>null</code>.
     * @return The {@link DataSetMetadata} instance parsed from stream or <code>null</code> if parameter is null. If
     * stream is empty, also returns <code>null</code>.
     */
    public DataSet from(InputStream json) {
        try {
            JsonParser parser = mapper.getFactory().createParser(json);
            return mapper.readerFor(DataSet.class).readValue(parser);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Writes the current {@link DataSetMetadata} to <code>writer</code> as JSON format.
     *
     * @param writer A non-null writer.
     */
    public void to(DataSet dataSet, Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null.");
        }
        try {
            mapper.writer().writeValue(writer, dataSet);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
    }

    @Test
    public void testArguments() throws Exception {
        try {
            from(null);
            fail("Expected an JSON parse exception.");
        } catch (Exception e) {
            assertThat(e, instanceOf(TalendRuntimeException.class));
            assertEquals(CommonErrorCodes.UNABLE_TO_PARSE_JSON, ((TalendRuntimeException) e).getCode());
        }
        try {
            from(new ByteArrayInputStream(new byte[0]));
            fail("Expected an JSON parse exception.");
        } catch (Exception e) {
            assertThat(e, instanceOf(TalendRuntimeException.class));
            assertEquals(CommonErrorCodes.UNABLE_TO_PARSE_JSON, ((TalendRuntimeException) e).getCode());
        }
    }

    @Test
    public void testRead1() throws Exception {

        DataSet dataSet = from(this.getClass().getResourceAsStream("test1.json"));
        assertNotNull(dataSet);

        final DataSetMetadata metadata = dataSet.getMetadata();
        assertEquals("410d2196-8f90-478f-a817-7e8b6694ac91", metadata.getId());
        assertEquals("test", metadata.getName());
        assertEquals("anonymousUser", metadata.getAuthor());
        assertEquals(2, metadata.getContent().getNbRecords());
        assertEquals(1, metadata.getContent().getNbLinesInHeader());
        assertEquals(0, metadata.getContent().getNbLinesInFooter());

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date expectedDate = dateFormat.parse("02-17-2015 09:02");
        assertEquals(expectedDate, new Date(metadata.getCreationDate()));

        List<ColumnMetadata> columns = dataSet.getMetadata().getRowMetadata().getColumns();
        assertEquals(6, columns.size());

        ColumnMetadata firstColumn = columns.get(0);
        assertEquals("0001", firstColumn.getId());
        assertEquals("id", firstColumn.getName());
        assertEquals("integer", firstColumn.getType());
        assertEquals(20, firstColumn.getQuality().getEmpty());
        assertEquals(26, firstColumn.getQuality().getInvalid());
        assertEquals(54, firstColumn.getQuality().getValid());

        ColumnMetadata lastColumn = columns.get(5);
        assertEquals("0007", lastColumn.getId());
        assertEquals("string", lastColumn.getType());
        assertEquals(8, lastColumn.getQuality().getEmpty());
        assertEquals(25, lastColumn.getQuality().getInvalid());
        assertEquals(67, lastColumn.getQuality().getValid());
    }

    @Test
    public void testWrite1() throws Exception {
        final ColumnMetadata.Builder columnBuilder = ColumnMetadata.Builder //
                .column() //
                .id(5) //
                .name("column1") //
                .type(Type.STRING) //
                .empty(0) //
                .invalid(10) //
                .valid(50);

        DataSetMetadata metadata = metadataBuilder.metadata().id("1234").name("name").author("author").created(0)
                .row(columnBuilder).build();

        final DataSetContent content = metadata.getContent();
        content.addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ",");
        content.setFormatFamilyId(new CSVFormatFamily().getBeanId());
        content.setMediaType("text/csv");
        metadata.getLifecycle().qualityAnalyzed(true);
        metadata.getLifecycle().schemaAnalyzed(true);
        LocalStoreLocation location = new LocalStoreLocation();
        metadata.setLocation(location);
        StringWriter writer = new StringWriter();
        DataSet dataSet = new DataSet();
        dataSet.setMetadata(metadata);
        to(dataSet, writer);
        assertThat(writer.toString(), sameJSONAsFile(DataSetJSONTest.class.getResourceAsStream("test2.json")));
    }

    @Test
    public void testRoundTrip() throws Exception {
        DataSet dataSet = from(DataSetJSONTest.class.getResourceAsStream("test3.json"));
        final DataSetMetadata metadata = dataSet.getMetadata();
        metadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ",");
        metadata.getContent().setFormatFamilyId(new CSVFormatFamily().getBeanId());
        assertNotNull(metadata);
        StringWriter writer = new StringWriter();
        to(dataSet, writer);
        assertThat(writer.toString(), sameJSONAsFile(DataSetJSONTest.class.getResourceAsStream("test3.json")));
    }

    @Test
    public void testColumnAtEnd() throws Exception {
        DataSet dataSet = from(this.getClass().getResourceAsStream("test4.json"));
        // There are 4 columns, but Jackson doesn't take them into account if at end of content. This is not "expected"
        // but known. This test ensure the known behavior remains the same.
        assertThat(dataSet.getMetadata(), nullValue());
    }

    @Test
    public void shouldDealWithNoRecords() throws Exception {
        // given
        final InputStream input = this.getClass().getResourceAsStream("no_records.json");

        // when
        DataSet dataSet = from(input);

        // then
        final List<DataSetRow> records = dataSet.getRecords().collect(Collectors.toList());
        assertTrue(records.isEmpty());
    }

    @Test
    public void should_iterate_row_with_metadata() throws IOException {
        // given
        String[] columnNames = new String[] { "0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009" };

        final InputStream input = this.getClass().getResourceAsStream("dataSetRowMetadata.json");
        try (JsonParser parser = mapper.getFactory().createParser(input)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            final Iterator<DataSetRow> iterator = dataSet.getRecords().iterator();

            List<ColumnMetadata> actualColumns = new ArrayList<>();
            int recordCount = 0;
            while (iterator.hasNext()) {
                final DataSetRow next = iterator.next();
                actualColumns = next.getRowMetadata().getColumns();
                assertThat(actualColumns, not(empty()));
                recordCount++;
            }

            // then
            assertEquals(10, recordCount);
            for (int i = 0; i < actualColumns.size(); i++) {
                final ColumnMetadata column = actualColumns.get(i);
                assertEquals(columnNames[i], column.getId());
            }
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

}
