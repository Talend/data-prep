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

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.error.ErrorList.ServiceType.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.api.service.command.error.ErrorList;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.metrics.Timed;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;

/**
 * Common API that does not stand in either DataSet, Preparation nor Transform.
 */
@RestController
public class CommonAPI extends APIService {

    @Autowired
    private ObjectMapper mapper;

    /**
     * Describe the supported error codes.
     *
     * @param output the http response.
     */
    @RequestMapping(value = "/api/errors", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all supported errors.", notes = "Returns the list of all supported errors.")
    @Timed
    public void listErrors(final OutputStream output) throws IOException {

        LOG.debug("Listing supported error codes");

        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(output);
        generator.setCodec(mapper);

        // start the errors array
        generator.writeStartArray();

        // write the direct known errors
        writeErrorsFromEnum(generator, CommonErrorCodes.values());
        writeErrorsFromEnum(generator, APIErrorCodes.values());

        // get dataset api errors
        HystrixCommand<InputStream> datasetErrors = getCommand(ErrorList.class, GenericCommand.DATASET_GROUP, DATASET);
        try (InputStream errorsInput = datasetErrors.execute()) {
            writeErrorsFromApi(generator, errorsInput);
        }

        // get preparation api errors
        HystrixCommand<InputStream> preparationErrors = getCommand(ErrorList.class, GenericCommand.PREPARATION_GROUP, PREPARATION);
        try (InputStream errorsInput = preparationErrors.execute()) {
            writeErrorsFromApi(generator, errorsInput);
        }

        // get transformation api errors
        HystrixCommand<InputStream> transformationErrors = getCommand(ErrorList.class, GenericCommand.TRANSFORM_GROUP, TRANSFORMATION);
        try (InputStream errorsInput = transformationErrors.execute()) {
            writeErrorsFromApi(generator, errorsInput);
        }

        // close the errors array
        generator.writeEndArray();
        generator.flush();
    }

    /**
     * Describe the supported Types
     */
    @RequestMapping(value = "/api/types", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all types.")
    @Timed
    public Type[] listTypes() {
        LOG.debug("Listing supported types");
        return Arrays.stream(Type.values()) //
                .filter(type -> type != Type.UTC_DATETIME) //
                .collect(Collectors.toList()) //
                .toArray(new Type[0]);
    }

    /**
     * Write the given error codes to the generator.
     *
     * @param generator the json generator to use.
     * @param codes the error codes to write.
     * @throws IOException if an error occurs.
     */
    private void writeErrorsFromEnum(JsonGenerator generator, ErrorCode[] codes) throws IOException {
        for (ErrorCode code : codes) {
            // cast to JsonErrorCode needed to ease json handling
            JsonErrorCodeDescription description = new JsonErrorCodeDescription(code);
            generator.writeObject(description);
        }
    }

    /**
     * Write the given error codes to the generator.
     *
     * @param generator the json generator to use.
     * @param input the error codes to write to read from the input stream.
     * @throws IOException if an error occurs.
     */
    private void writeErrorsFromApi(JsonGenerator generator, InputStream input) throws IOException {
        Iterator<JsonErrorCodeDescription> iterator = mapper.readerFor(JsonErrorCodeDescription.class).readValues(input);
        while (iterator.hasNext()) {
            final JsonErrorCodeDescription description = iterator.next();
            generator.writeObject(description);
        }
    }
}
