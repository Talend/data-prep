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

package org.talend.dataprep.command.dataset;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.adapter.EncodedSample;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.util.avro.AvroReader;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT;

/**
 * Command to get a dataset.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetGet extends GenericCommand<InputStream> {

    private static final BasicHeader ACCEPT_HEADER =
            new BasicHeader(ACCEPT, APPLICATION_JSON.withCharset(UTF_8).toString());

    private final boolean fullContent;

    private final String dataSetId;

    private final boolean includeInternalContent;

    private final boolean includeMetadata;

    private final String filter;

    @Autowired
    private DataSetContentLimit limit;

    public DataSetGet(final String dataSetId, final boolean fullContent, final boolean includeInternalContent) {
        this(dataSetId, fullContent, includeInternalContent, EMPTY);
    }

    public DataSetGet(final String dataSetId, final boolean fullContent, final boolean includeInternalContent, String filter) {
        this(dataSetId, fullContent, includeInternalContent, filter, true);
    }

    public DataSetGet(final String dataSetId, final boolean fullContent, final boolean includeInternalContent, String filter, final boolean includeMetadata) {
        super(DATASET_GROUP);
        this.fullContent = fullContent;
        this.dataSetId = dataSetId;
        this.includeInternalContent = includeInternalContent;
        this.includeMetadata = includeMetadata;
        this.filter = filter;

        on(HttpStatus.NO_CONTENT).then(emptyStream());
        on(HttpStatus.OK).then(this::readResult);
        onError(e -> new TDPException(UNABLE_TO_RETRIEVE_DATASET_CONTENT, e, build().put("id", dataSetId)));
    }

    @PostConstruct
    private void initConfiguration() {
        execute(() -> {
            URI build;
            try {
                build = new URIBuilder(datasetServiceUrl + "/api/v1/dataset-sample/" + dataSetId).build();
            } catch (URISyntaxException e) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }

            HttpGet httpGet = new HttpGet(build);
            httpGet.addHeader(ACCEPT_HEADER);
            return httpGet;
        });
    }

    private InputStream readResult(HttpRequestBase httpRequestBase, HttpResponse httpResponse) {
        try {
            InputStream content = httpResponse.getEntity().getContent();
            EncodedSample encodedSample = objectMapper.readValue(content, EncodedSample.class);

            // parse input data
            Schema schema = new Schema.Parser().parse(encodedSample.getSchema().toString());
            StringBuilder avroRecordsString = new StringBuilder();
            encodedSample.getData().forEach(jn -> avroRecordsString.append(jn.toString()));

            AvroReader avroReader =
                    new AvroReader(new ByteArrayInputStream(avroRecordsString.toString().getBytes(UTF_8)), schema);

            RowMetadata rowMetadata = RowMetadataUtils.toRowMetadata(schema);
            List<DataSetRow> rows = new ArrayList<>();
            long rowId = 0;
            while (avroReader.hasNext()) {
                GenericRecord nextRecord = avroReader.next();
                DataSetRow dataSetRow = new DataSetRow(rowMetadata);
                for (ColumnMetadata cm : rowMetadata.getColumns()) {
                    dataSetRow.set(cm.getId(), toString(nextRecord, cm));
                }
                dataSetRow.setTdpId(rowId++);
                rows.add(dataSetRow);
            }

            // build dataset object
            DataSet dataSet = new DataSet();
            DataSetMetadata dataSetMetadata = new DataSetMetadata();
            dataSetMetadata.setId(dataSetId);
            dataSetMetadata.setRowMetadata(rowMetadata);
            dataSet.setMetadata(dataSetMetadata);
            dataSet.setRecords(rows.stream());

            // Write all in a buffer
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                objectMapper.writeValue(outputStream, dataSet);
                return new ByteArrayInputStream(outputStream.toByteArray());
            }
        } catch (IOException e) {
            throw new TalendRuntimeException(org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * From Avro value to dataprep string.
     *
     * @param currentRecord avro record
     * @param column dataprep column
     * @return row value
     */
    private static String toString(GenericRecord currentRecord, ColumnMetadata column) {
        final Schema fieldSchema = currentRecord.getSchema().getField(column.getName()).schema();
        Object recordFieldValue = currentRecord.get(column.getName());
        return convertAvroFieldToString(fieldSchema, recordFieldValue);
    }

    private static String convertAvroFieldToString(Schema fieldSchema, Object recordFieldValue) {
        final String result;
        switch (fieldSchema.getType()) {
        case BYTES:
            result = new String(((ByteBuffer) recordFieldValue).array());
            break;
        case UNION:
            String unionValue = EMPTY;
            Iterator<Schema> iterator = fieldSchema.getTypes().iterator();
            while (EMPTY.equals(unionValue) && iterator.hasNext()) {
                Schema schema = iterator.next();
                unionValue = convertAvroFieldToString(schema, recordFieldValue);
            }
            result = unionValue;
            break;
        case STRING:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case BOOLEAN:
        case ENUM:
            result = String.valueOf(recordFieldValue);
            break;
        case NULL:
            result = EMPTY;
            break;
        default: // RECORD, ARRAY, MAP, FIXED
            result = "Data Preparation cannot interpret this value";
            break;
        }
        return result;
    }
}
