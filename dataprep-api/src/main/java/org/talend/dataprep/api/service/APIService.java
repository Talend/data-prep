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

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.Api;

@Api(value = "api", basePath = "/api", description = "Data Preparation API")
public class APIService {

    protected static final Logger LOG = LoggerFactory.getLogger(APIService.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private PoolingHttpClientConnectionManager connectionManager;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected BeanConversionService beanConversionService;

    protected <T extends HystrixCommand> T getCommand(Class<T> clazz, Object... args) {
        try {
            return context.getBean(clazz, args);
        } catch (BeansException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_FIND_COMMAND, e, ExceptionContext.build().put("class", clazz)
                    .put("args", args));
        }
    }

    /**
     * @return the connection pool stats.
     */
    protected PoolStats getConnectionStats() {
        return connectionManager.getTotalStats();
    }
}
