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

package org.talend.dataprep.api.service.delegate;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

public abstract class AbstractSearchDelegate<T> implements SearchDelegate<T> {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ApplicationContext context;

    protected <S extends HystrixCommand> S getCommand(Class<S> clazz, Object... args) {
        try {
            return context.getBean(clazz, args);
        } catch (BeansException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_FIND_COMMAND, e, ExceptionContext.build().put("class", clazz)
                    .put("args", args));
        }
    }
}
