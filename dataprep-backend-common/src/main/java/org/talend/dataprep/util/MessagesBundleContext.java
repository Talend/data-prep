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

package org.talend.dataprep.util;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.i18n.MessagesBundle;

/**
 * This bean and servlet Filter will hold a ThreadLocal variable with a reference to the bean {@link MessagesBundle}
 */
@Component
public class MessagesBundleContext implements Filter {

    private static final ThreadLocal<MessagesBundle> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private MessagesBundle messagesBundle;

    public static MessagesBundle get() {
        return THREAD_LOCAL.get();
    }

    /**
     * will setup the current thread. Can help for startup thread (and ease unit testing as well)
     */
    @PostConstruct
    public void initializeCurrentThread() {
        THREAD_LOCAL.set(messagesBundle);
    }

    @Override
    public void destroy() {
        // really to be sure it's clean because doFilter do it as well.
        THREAD_LOCAL.remove();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        THREAD_LOCAL.set( messagesBundle );
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            THREAD_LOCAL.remove();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no op
    }
}
