package org.talend.dataprep.api.service.command.common;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;

import io.swagger.annotations.ApiOperation;

@RestController
public class GenericCommandTestService {

    @RequestMapping(value = "/command/test/success", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String success() throws IOException {
        return "success";
    }

    @RequestMapping(value = "/command/test/success_with_unknown", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String success_with_unknown() throws IOException {
        HttpResponseContext.status(HttpStatus.ACCEPTED);
        return "success";
    }

    @RequestMapping(value = "/command/test/fail_with_400", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fail_with_400() throws IOException {
        throw new TDPException(CommonErrorCodes.MISSING_ACTION_SCOPE);
    }

    @RequestMapping(value = "/command/test/fail_with_500", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fail_with_500() throws IOException {
        throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON);
    }

    @RequestMapping(value = "/command/test/fail_with_unknown", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public void fail_with_unknown() throws IOException {
        HttpResponseContext.status(HttpStatus.I_AM_A_TEAPOT);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/unexpected", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public void fail_with_unexpected() {
        throw new IndexOutOfBoundsException();
    }

}
