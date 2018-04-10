/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.talend.dataprep.exception.TdpExceptionDto;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ HttpClientErrorException.class })
    public ResponseEntity<TdpExceptionDto> handleError(HttpClientErrorException e) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        String code = e.getStatusCode().toString();
        String message = e.getMessage();
        String localizedMessage = e.getLocalizedMessage();
        String statusText = e.getStatusText();

        TdpExceptionDto exceptionDto = new TdpExceptionDto(code, null, message, localizedMessage, statusText, null);

        return new ResponseEntity<>(exceptionDto, httpHeaders, e.getStatusCode());
    }

}
