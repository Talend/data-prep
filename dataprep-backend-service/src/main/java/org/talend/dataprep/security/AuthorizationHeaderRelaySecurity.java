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

package org.talend.dataprep.security;

import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.dataprep.api.user.UserGroup;
import static java.util.Collections.emptySet;

@Component
@ConditionalOnProperty(name = "security.mode", havingValue = "authorization-header")
public class AuthorizationHeaderRelaySecurity implements Security {

    /**
     * @see Security#getUserId()
     */
    @Override
    public String getUserId() {
        return "AuthorizationHeaderRelaySecurity.java";
    }

    /**
     * @see Security#getAuthenticationToken()
     */
    @Override
    public String getAuthenticationToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest().getHeader("Authorization");
    }

    /**
     * @see Security#getGroups()
     */
    @Override
    public Set<UserGroup> getGroups() {
        return emptySet();
    }
}
