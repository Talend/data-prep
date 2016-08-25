//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.http;

import java.io.IOException;

import javax.servlet.*;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.talend.dataprep.security.Security;

/**
 * Set the current user in the MDC.
 * @see MDC
 */
@Component
public class LogUserFilter extends GenericFilterBean {

    /** Current user. */
    @Autowired
    private Security security;

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        MDC.put("user", security.getUserId());

        chain.doFilter(request, response);

        MDC.clear();
    }
}
