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

package org.talend.dataprep.api.service.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

public class APIDelegate {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected ObjectMapper mapper;

    protected <T extends HystrixCommand> T getCommand(Class<T> clazz, Object... args) {
        try {
            return context.getBean(clazz, args);
        } catch (BeansException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_FIND_COMMAND, e,
                    ExceptionContext.build().put("class", clazz).put("args", args));
        }
    }

}
