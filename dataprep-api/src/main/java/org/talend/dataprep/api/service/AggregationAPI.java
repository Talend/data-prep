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

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.InputStream;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.service.command.aggregation.Aggregate;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * High level Aggregation API.
 */
@RestController
@Api(value = "api", basePath = "/api", description = "Aggregation API")
public class AggregationAPI extends APIService {

    /**
     * Compute an aggregation according to the given parameters.
     *
     * @param input The aggregation parameters.
     */
    @RequestMapping(value = "/api/aggregate", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Compute aggregation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, notes = "Compute aggregation according to the given parameters")
    public ResponseEntity<StreamingResponseBody> compute(@RequestBody @Valid final AggregationParameters input) {
        LOG.debug("Aggregation computation requested (pool: {} )...", getConnectionStats());
        // get the command and execute it, then copy the content to the http response
        try {
            GenericCommand<InputStream> command = getCommand(Aggregate.class, input);
            return CommandHelper.toStreaming(command);
        } finally {
            LOG.debug("Aggregation done (pool: {} )...", getConnectionStats());
        }
    }

}
