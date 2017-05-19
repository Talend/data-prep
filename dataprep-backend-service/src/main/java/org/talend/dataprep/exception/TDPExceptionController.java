// ============================================================================
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.conversions.BeanConversionService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller advice applied to all controllers so that they can handle TDPExceptions.
 */
@ControllerAdvice
public class TDPExceptionController {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TDPExceptionController.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BeanConversionService conversionService;

    /**
     * Send the TDPException into the http response.
     *
     * @param e the TDP exception.
     * @return the http response.
     */
    @ExceptionHandler({ TalendRuntimeException.class })
    public ResponseEntity<String> handleError(TalendRuntimeException e) throws JsonProcessingException {
        if (e instanceof TDPException) {
            LOGGER.error("An error occurred", e);
        } else {
            LOGGER.debug("Returning an exception to HTTP client.", e);
        }
        HttpHeaders httpStatus = new HttpHeaders();
        httpStatus.setContentType(MediaType.APPLICATION_JSON_UTF8);
        TdpExceptionDto exceptionDto = conversionService.convert(e, TdpExceptionDto.class);
        return new ResponseEntity<>(objectMapper.writeValueAsString(exceptionDto), httpStatus, HttpStatus.valueOf(e.getCode().getHttpStatus()));
    }

}
