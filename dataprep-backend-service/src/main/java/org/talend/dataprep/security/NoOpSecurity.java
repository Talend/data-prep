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

package org.talend.dataprep.security;

import static java.util.Collections.emptySet;

import java.util.Locale;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.analytics.Analytics;
import org.talend.dataprep.api.user.UserGroup;

@Component
@ConditionalOnProperty(name = "security.mode", havingValue = "none", matchIfMissing = true)
public class NoOpSecurity implements Security {

    /**
     * @see Security#getUserId()
     */
    @Override
    public String getUserId() {
        return System.getProperty("user.name");
    }

    /**
     * @see Security#getAuthenticationToken()
     */
    @Override
    public String getAuthenticationToken() {
        // no token in the Free Desktop edition
        return null;
    }

    /**
     * @see Security#getGroups()
     */
    @Override
    public Set<UserGroup> getGroups() {
        return emptySet();
    }

    @Override
    public String getTenantId() {
        return "none";
    }

    /**
     * @return the tenant name.
     */
    @Override
    public String getTenantName() {
        return "none";
    }

    @Override
    public boolean isTDPUser() {
        return true;
    }

    @Override
    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    public Analytics getAnalytics() {
        return null;
    }

    @Override
    public Set<String> getApplications() {
        return emptySet();
    }
}
