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
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;

import org.apache.avro.Schema;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

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

        onError(Defaults.passthrough());
        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    private void initConfiguration() {
        URI datasetURI;
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            datasetURI = uriBuilder.setPath(uriBuilder.getPath() + "/api/v1/datasets/" + dataSetId + "/schema").build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        execute(() -> new HttpGet(datasetURI));

        on(HttpStatus.OK).then((req, res) -> {
            try {
                String schemaAsString = EntityUtils.toString(res.getEntity(), UTF_8);
                return new Schema.Parser().parse(schemaAsString);
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                EntityUtils.consumeQuietly(res.getEntity());
                req.releaseConnection();
            }
        });
    }

}
