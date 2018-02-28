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

package org.talend.dataprep.api.service.command;

import java.util.Collections;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.talend.dataprep.command.GenericCommand;

import com.netflix.hystrix.HystrixCommandGroupKey;

@Component
@Scope("prototype")
public class AsyncGenericCommand<T> extends GenericCommand<ResponseEntity<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncGenericCommand.class);

    /**
     * Protected constructor.
     *
     * @param group the command group.
     */
    protected AsyncGenericCommand(HystrixCommandGroupKey group) {
        super(group);
        on(HttpStatus.ACCEPTED).then((req, res) -> {
            final MultiValueMap<String, String> headers = new HttpHeaders();
            for (Header header : res.getAllHeaders()) {
                headers.put(header.getName(), Collections.singletonList(header.getValue()));
            }
            return new ResponseEntity(null, headers, HttpStatus.ACCEPTED);

        });
        on(HttpStatus.NO_CONTENT).then((req, resp) -> {
            resp.setStatusCode(HttpStatus.NO_CONTENT.value());
            return null;
        });
    }
}
