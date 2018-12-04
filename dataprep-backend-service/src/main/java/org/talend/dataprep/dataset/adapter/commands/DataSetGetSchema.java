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

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;

import org.apache.avro.Schema;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.util.avro.AvroUtils;

/**
 * Get the dataSet schema.
 * 
 * @see Schema
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetGetSchema extends GenericCommand<Schema> {

    private final String dataSetId;

    /**
     * Private constructor to ensure the use of IoC
     *
     * @param dataSetId the dataset id to get.
     */
    private DataSetGetSchema(final String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        this.dataSetId = dataSetId;

        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    private void initConfiguration() {
        URI datasetURI;
        try {
            datasetURI = new URIBuilder(datasetServiceUrl + "/api/v1/datasets/" + dataSetId + "/schema").build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        execute(() -> {
            HttpGet httpGet = new HttpGet(datasetURI);
            httpGet.setHeader(ACCEPT, AvroUtils.AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE);
            return httpGet;
        });

        on(HttpStatus.OK).then((req, res) -> {
            try (InputStream inputStream = res.getEntity().getContent()) {
                return new Schema.Parser().parse(inputStream);
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        });
    }

}
