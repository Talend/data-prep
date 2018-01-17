//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.aggregation;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Aggregate command. Take the content of the dataset or preparation before sending it to the transformation service.
 */
@Component
@Scope("request")
public class Aggregate extends GenericCommand<InputStream> {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);

    /**
     * Default constructor.
     * @param parameters aggregation parameters.
     */
    public Aggregate(final AggregationParameters parameters) {
        super(GenericCommand.TRANSFORM_GROUP);
        execute(() -> onExecute(parameters));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * Call the transformation service with the export parameters in json the request body.
     *
     * @param parameters the aggregate parameters.
     * @return the http request to execute.
     */
    private HttpRequestBase onExecute(AggregationParameters parameters) {
        // must work on either a dataset or a preparation, if both parameters are set, an error is thrown
        if (StringUtils.isNotBlank(parameters.getDatasetId()) && StringUtils.isNotBlank(parameters.getPreparationId())) {
            LOG.error("Cannot aggregate on both dataset id & preparation id : {}", parameters);
            throw new TDPException(CommonErrorCodes.BAD_AGGREGATION_PARAMETERS);
        }

        String uri = transformationServiceUrl + "/aggregate"; //$NON-NLS-1$
        HttpPost aggregateCall = new HttpPost(uri);

        try {
            String paramsAsJson = objectMapper.writer().writeValueAsString(parameters);
            aggregateCall.setEntity(new StringEntity(paramsAsJson, ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_AGGREGATE, e);
        }

        return aggregateCall;
    }

}
