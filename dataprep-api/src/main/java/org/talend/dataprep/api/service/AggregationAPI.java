package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.command.aggregation.Aggregate;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

import com.netflix.hystrix.HystrixCommand;

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
    public void compute(@RequestBody @Valid final AggregationParameters input, final OutputStream output) {

        LOG.debug("Aggregation computation requested (pool: {} )...", getConnectionStats());

        // get the command and execute it
        HttpClient client = getClient();
        HystrixCommand<InputStream> command = getCommand(Aggregate.class, client, input);

        // copy the content to the http response
        try (InputStream result = command.execute()) {
            IOUtils.copyLarge(result, output);
            output.flush();
            LOG.debug("Aggregation done (pool: {} )...", getConnectionStats());
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

    }

}
