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

import org.talend.dataprep.api.service.settings.uris.api.UriSettings;

/**
 * Home elements configuration
 */

// @formatter:off
//@Configuration
public interface Uris {

    //@Value("${security.oauth2.logout.uri:/logout}")
    //String logoutUri;

    // {serverUrl}/logout
    UriSettings LOGOUT_URI =
            UriSettings.builder()
                    .name("logout")
                    .uri("/logout")
                            .build();

    // {serverUrl}/api/user
    UriSettings API_USER_URI =
            UriSettings.builder()
                    .name("api_user")
                    .uri("/api/user")
                    .build();

    // {serverUrl}/api/share
    UriSettings API_SHARE_URI =
            UriSettings.builder()
                    .name("api_share")
                    .uri("/api/share")
                    .build();

   //  {this.shareUrl}/groups
    UriSettings GROUPS_URI =
            UriSettings.builder()
                    .name("group")
                    .uri("/group")
                    .build();

    //{this.shareUrl}/users
    UriSettings USERS_URI =
            UriSettings.builder()
                    .name("share")
                    .uri("/api/share")
                    .build();
    //{serverUrl}/api/export/async
    UriSettings API_EXPORT_ASYNC_URI =
            UriSettings.builder()
                    .name("export_async")
                    .uri("/api/export/async")
                    .build();
    //{serverUrl}/api
    UriSettings API_URI =
            UriSettings.builder()
                    .name("api")
                    .uri("/api")
                    .build();

    //{serverUrl}/api
    UriSettings LOGOUT_REDIRECTION_URI =
            UriSettings.builder()
                    .name("logout_redirection")
                    .uri("http://localhost:9080/oidc/idp/logout?client_id=J3Wqyvlzgw0ySg&post_logout_redirect_uri=http://localhost:3000/")
                    .build();
}
// @formatter:on
