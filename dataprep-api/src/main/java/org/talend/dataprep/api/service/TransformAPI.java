//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.api.DynamicParamsInput;
import org.talend.dataprep.api.service.command.dataset.DataSetGet;
import org.talend.dataprep.api.service.command.preparation.PreparationGetContent;
import org.talend.dataprep.api.service.command.transformation.ColumnActions;
import org.talend.dataprep.api.service.command.transformation.LineActions;
import org.talend.dataprep.api.service.command.transformation.SuggestActionParams;
import org.talend.dataprep.api.service.command.transformation.SuggestColumnActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class TransformAPI extends APIService {


    /**
     * Get all the possible actions for a given column.
     *
     * Although not rest compliant, this is done via a post in order to pass all the column metadata in the request body
     * without risking breaking the url size limit if GET would be used.
     *
     * @param body the column description (json encoded) in the request body.
     */
    @RequestMapping(value = "/api/transform/actions/column", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all actions for a data set column.", notes = "Returns all actions for the given column.")
    @Timed
    public void columnActions(@ApiParam(value = "Optional column Metadata content as JSON") InputStream body,
                                     final OutputStream output) {

        HttpClient client = getClient();

        // Asks transformation service for all actions for column type and domain
        HystrixCommand<InputStream> getSuggestedActions = getCommand(ColumnActions.class, client, body);
        // Returns actions
        try {
            // olamy: this is weird to have to configure that manually whereas there is an annotation for the method!!
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(getSuggestedActions.execute(), output);
            output.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Suggest the possible actions for a given column.
     *
     * Although not rest compliant, this is done via a post in order to pass all the column metadata in the request body
     * without risking breaking the url size limit if GET would be used.
     *
     * @param body the column description (json encoded) in the request body.
     */
    @RequestMapping(value = "/api/transform/suggest/column", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a data set column.", notes = "Returns the suggested actions for the given column in decreasing order of likeness.")
    @Timed
    public void suggestColumnActions(@ApiParam(value = "Column Metadata content as JSON") InputStream body,
            final OutputStream output) {

        HttpClient client = getClient();

        // Asks transformation service for suggested actions for column type and domain
        HystrixCommand<InputStream> getSuggestedActions = getCommand(SuggestColumnActions.class, client, body);
        // Returns actions
        try {
            // olamy: this is weird to have to configure that manually whereas there is an annotation for the method!!
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(getSuggestedActions.execute(), output);
            output.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get all the possible actions available on lines.
     */
    @RequestMapping(value = "/api/transform/actions/line", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all actions on line", notes = "Returns all actions for the given column.")
    @Timed
    public void lineActions(OutputStream output) {
        final HttpClient client = getClient();
        final HystrixCommand<InputStream> getSuggestedActions = getCommand(LineActions.class, client);
        try {
            IOUtils.copyLarge(getSuggestedActions.execute(), output);
            output.flush();
        } catch (final IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get the suggested action dynamic params. Dynamic params depends on the context (dataset / preparation / actual
     * transformations)
     */
    @RequestMapping(value = "/api/transform/suggest/{action}/params", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the transformation dynamic parameters", notes = "Returns the transformation parameters.")
    @Timed
    public void suggestActionParams(@ApiParam(value = "Transformation name.")
    @PathVariable("action")
    final String action, @ApiParam(value = "Suggested dynamic transformation input (preparation id or dataset id")
    @Valid
    final DynamicParamsInput dynamicParamsInput, final OutputStream output) {

        try {
            // get preparation/dataset content
            HystrixCommand<InputStream> inputData;
            if (isNotBlank(dynamicParamsInput.getPreparationId())) {
                inputData = getCommand(PreparationGetContent.class, getClient(), dynamicParamsInput.getPreparationId(), dynamicParamsInput.getStepId());
            } else {
                inputData = getCommand(DataSetGet.class, getClient(), dynamicParamsInput.getDatasetId(), true, null);
            }

            // get params, passing content in the body
            final HystrixCommand<InputStream> getActionDynamicParams = getCommand(SuggestActionParams.class, getClient(),
                    inputData, action, dynamicParamsInput.getColumnId());


            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            // trigger calls and return last execute content
            IOUtils.copyLarge(getActionDynamicParams.execute(), output);
            output.flush();
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_DYNAMIC_ACTION_PARAMS, e);
        }
    }
}
