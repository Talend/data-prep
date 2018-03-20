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

package org.talend.dataprep.command;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import io.swagger.annotations.ApiOperation;

@RestController
public class GenericCommandTestService {

    @RequestMapping(value = "/command/test/success", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String success() throws IOException {
        return "success";
    }

    @RequestMapping(value = "/command/test/authentication/token", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testToken(@RequestHeader(value= AUTHORIZATION) String token) throws IOException {
        if (StringUtils.isBlank(token)) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
        }
        return token;
    }

    @RequestMapping(value = "/command/test/success_with_unknown", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String success_with_unknown() throws IOException {
        HttpResponseContext.status(HttpStatus.ACCEPTED);
        return "success";
    }

    @RequestMapping(value = "/command/test/fail_with_400", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fail_with_400() throws IOException {
        throw new TDPException(BaseErrorCodes.MISSING_ACTION_SCOPE);
    }

    @RequestMapping(value = "/command/test/fail_with_500", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String fail_with_500() throws IOException {
        throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON);
    }

    @RequestMapping(value = "/command/test/fail_with_unknown", method = RequestMethod.GET)
    public void fail_with_unknown() throws IOException {
        HttpResponseContext.status(HttpStatus.I_AM_A_TEAPOT);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/unexpected", method = RequestMethod.GET)
    public void fail_with_unexpected() {
        throw new IndexOutOfBoundsException();
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_bad_request_exception", method = RequestMethod.GET)
    public void fail_with_bad_request_exception() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION, null, "Bad request", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_command_exception", method = RequestMethod.GET)
    public void fail_with_command_exception() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION, null, "Command Exception", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_rejected_semaphore_execution", method = RequestMethod.GET)
    public void fail_with_rejected_semaphore_execution() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.REJECTED_SEMAPHORE_EXECUTION, null, "Rejected Semaphore execution", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_rejected_execution", method = RequestMethod.GET)
    public void fail_with_rejected_thread_execution() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.REJECTED_THREAD_EXECUTION, null, "Rejected thread execution", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_rejected_semaphore_fallback", method = RequestMethod.GET)
    public void fail_with_rejected_semaphore_fallback() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.REJECTED_SEMAPHORE_FALLBACK, null, "Rejected semaphore fallback", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_short_circuit", method = RequestMethod.GET)
    public void fail_with_short_circuit() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.SHORTCIRCUIT, null, "Short circuit", null, null);
    }

    @ApiOperation("Execute an operation")
    @RequestMapping(value = "/command/test/fail_timeout", method = RequestMethod.GET)
    public void fail_with_timeout() {
        throw new HystrixRuntimeException(HystrixRuntimeException.FailureType.TIMEOUT, null, "Timeout", null, null);
    }

}
