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

package org.talend.dataprep.api.service.settings.uris.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.uris.api.UriSettings;

/**
 * Uris elements configuration for build
 */

@Component
public class UrisBuilder {

    @Value("${security.oauth2.logout.uri:/logout}")
    private String logoutUri;

    @Value("${security.oauth2.login.uri:/login}")
    private String loginUri;

    @Value("${security.oauth2.client.clientId:/}")
    private String clientId;

    @Value("${security.oidc.client.endSessionEndpoint:/}")
    private String endSessionEndpoint;

    @Value("${security.postLogoutRedirect:/}")
    private String postLogoutRedirect;

    public UriSettings getLogoutUriSettings() {
        return UriSettings.builder()
                .name("logout")
                .uri(logoutUri)
                .build();
    }

    public UriSettings getLoginUriSettings() {
        return UriSettings.builder()
                .name("login")
                .uri(loginUri)
                .build();
    }

    public UriSettings getLogoutUriRedirectSettings() {
        return UriSettings.builder()
                .name("logoutRedirect")
                .uri(buildLogoutUrl())
                .build();
    }

    private String buildLogoutUrl() {
        StringBuilder url = new StringBuilder();
        url= endSessionEndpoint.isEmpty()? url.append("") : url.append(endSessionEndpoint).append("?");
        url= clientId.isEmpty()? url.append("") : url.append("client_id=").append(clientId).append("&");
        url= clientId.isEmpty()? url.append("") : url.append("post_logout_redirect_uri=").append(postLogoutRedirect);
        return url.toString();
    }
}
// @formatter:on
