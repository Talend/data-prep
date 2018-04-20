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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.adapter.EncodedSample;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.util.avro.AvroReader;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.nio.charset.StandardCharsets.UTF_8;
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
        this(dataSetId, fullContent, includeInternalContent, StringUtils.EMPTY);
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
        on(HttpStatus.OK).then((httpRequestBase, httpResponse) -> {
            try {
                InputStream content = httpResponse.getEntity().getContent();
                EncodedSample encodedSample = objectMapper.readValue(content, EncodedSample.class);

                ObjectNode schemaAsJackson = encodedSample.getSchema();
                String s = schemaAsJackson.toString();
                // TODO remove this quick and dirty fix
                s = s.replaceAll("\"type\":\"bytes\"", "\"type\":[\"null\", \"bytes\"] ");

                Schema schema = new Schema.Parser().parse(s);

                StringBuilder avroRecordsString = new StringBuilder();
                encodedSample.getData().forEach(jn -> avroRecordsString.append(jn.toString()).append(','));

                AvroReader avroReader =
                        new AvroReader(new ByteArrayInputStream(avroRecordsString.toString().getBytes(UTF_8)), schema);


                RowMetadata rowMetadata = RowMetadataUtils.toRowMetadata(schema);

                DataSet dataSet = new DataSet();

                DataSetMetadata dataSetMetadata = new DataSetMetadata();
                dataSetMetadata.setRowMetadata(rowMetadata);
                dataSet.setMetadata(dataSetMetadata);

                dataSet.setRecords(avroReader.asStream().map(genericRecord -> {
                    RowMetadataUtils.Metadata metadata = new RowMetadataUtils.Metadata(0L, rowMetadata.getColumns());
                    return RowMetadataUtils.toDataSetRow(genericRecord, metadata);
                }));

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    objectMapper.writeValue(outputStream, dataSet);
                    return new ByteArrayInputStream(outputStream.toByteArray());
                }
            } catch (IOException e) {
                throw new TalendRuntimeException(org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
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
}
