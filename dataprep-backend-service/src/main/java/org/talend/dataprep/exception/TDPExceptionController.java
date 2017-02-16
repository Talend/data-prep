// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.exception;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.http.HttpResponseContext;

/**
 * Controller advice applied to all controllers so that they can handle TDPExceptions.
 */
@ControllerAdvice
public class TDPExceptionController {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TDPExceptionController.class);

    /**
     * Send the TDPException into the http response.
     *
     * @param e the TDP exception.
     * @return the http response.
     */
    @ExceptionHandler({ TalendRuntimeException.class })
    @ResponseBody
    public String handleError(TalendRuntimeException e) {
        if (e instanceof TDPException) {
            final TDPException tdpException = (TDPException) e;
            if (!tdpException.isError()) {
                LOGGER.error("An error occurred", e);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("An error occurred", e);
            }
        }
        HttpResponseContext.status(HttpStatus.valueOf(e.getCode().getHttpStatus()));
        HttpResponseContext.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        final StringWriter message = new StringWriter();
        e.writeTo(message);
        return message.toString();
    }

}
