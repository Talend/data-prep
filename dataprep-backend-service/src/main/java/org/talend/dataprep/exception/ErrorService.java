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

package org.talend.dataprep.exception;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.http.HttpResponseContext;

@RestController
public class ErrorService {

    /**
     * This operation is called by Spring when unexpected (and Spring level) exceptions occur. This operation ensure
     * enough is passed to caller for diagnostic.
     *
     * @param request Current request from user.
     * @return A map containing more detailed information about the error (in JSON format).
     */
    @RequestMapping(value = "/error", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> handle(final HttpServletRequest request) {
        final int status = Integer.parseInt(request.getAttribute("javax.servlet.error.status_code").toString());
        final Object message = request.getAttribute("javax.servlet.error.message");

        final Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("reason", message);

        HttpResponseContext.status(HttpStatus.valueOf(status));

        return body;
    }

}
