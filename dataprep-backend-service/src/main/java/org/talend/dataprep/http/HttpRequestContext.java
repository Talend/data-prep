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

package org.talend.dataprep.http;

import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A helper class around {@link RequestContextHolder}: allow simple modifications on HTTP context without worrying
 * whether code is called in a web context or not.
 */
public class HttpRequestContext {

    /**
     * Default empty constructor.
     */
    private HttpRequestContext() {
        // private constructor for this utility class
    }

    public static Enumeration<String> parameters() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest().getParameterNames();
        }
        return Collections.emptyEnumeration();
    }

    public static String parameter(String parameterName) {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest().getParameter(parameterName);
        }
        return StringUtils.EMPTY;
    }

}
