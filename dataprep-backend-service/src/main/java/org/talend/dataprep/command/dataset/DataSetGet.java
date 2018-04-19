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
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;
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
        on(HttpStatus.OK).then(pipeStream());
        onError(e -> new TDPException(UNABLE_TO_RETRIEVE_DATASET_CONTENT, e, build().put("id", dataSetId)));
    }

    @PostConstruct
    private void initConfiguration() {
        if (limit.limitContentSize() || fullContent) {
            this.configureLimitedDataset(dataSetId);
        } else {
            this.configureSampleDataset(dataSetId);
        }
    }

    private void configureLimitedDataset(final String dataSetId) {
        execute(() -> {
            URI build;
            try {
                build = new URIBuilder(datasetServiceUrl + "/datasets/" + dataSetId + "/content") //
                        .addParameter("metadata", Boolean.toString(includeMetadata)) //
                        .addParameter("includeInternalContent", Boolean.toString(includeInternalContent)) //
                        .addParameter("filter", filter) //
                        .build();
            } catch (URISyntaxException e) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }

            HttpGet httpGet = new HttpGet(build);
            httpGet.addHeader(ACCEPT_HEADER);
            return httpGet;
        });
    }

    private void configureSampleDataset(final String dataSetId) {
        execute(() -> {
            URI build;
            try {
                build = new URIBuilder(datasetServiceUrl + "/datasets/" + dataSetId + "/sample") //
                        .addParameter("filter", filter) //
                        .build();
            } catch (URISyntaxException e) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }

            HttpGet httpGet = new HttpGet(build);
            httpGet.addHeader(ACCEPT_HEADER);
            return httpGet;
        });
    }
}
