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

package org.talend.dataprep.dataset.adapter.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.util.avro.AvroUtils;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT;
import static org.talend.dataprep.util.avro.AvroUtils.readBinaryStream;

/**
 * Command to get the stream of AVRO records of the dataset.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetGetContent extends GenericCommand<Stream<GenericRecord>> {

    private final String dataSetId;

    private final Schema contentSchema;

    private final Long limit;

    public DataSetGetContent(final String dataSetId, Schema contentSchema, Long limit) {
        super(DATASET_GROUP);
        this.dataSetId = dataSetId;
        this.contentSchema = contentSchema;
        this.limit = limit;

        on(HttpStatus.NO_CONTENT).then((req, resp) -> Stream.empty());
        on(HttpStatus.OK).then(this::readResult);
        onError(e -> new TDPException(UNABLE_TO_RETRIEVE_DATASET_CONTENT, e, build().put("id", dataSetId)));
    }

    @PostConstruct
    private void initConfiguration() {
        execute(() -> {
            URI uri;

            try {
                URIBuilder uriBuilder =
                        new URIBuilder(datasetServiceUrl + "/api/v1/datasets/" + dataSetId + "/content");
                if (limit != null) {
                    uriBuilder.addParameter("limit", Long.toString(limit));
                }
                uri = uriBuilder
                        .build();
            } catch (URISyntaxException e) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }

            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader(ACCEPT, AvroUtils.AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE);
            return httpGet;
        });
    }

    private Stream<GenericRecord> readResult(HttpRequestBase httpRequestBase, HttpResponse httpResponse) {
        try {
            InputStream content = httpResponse.getEntity().getContent();
            return readBinaryStream(content, contentSchema).asStream();
        } catch (IOException e) {
            throw new TalendRuntimeException(org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
