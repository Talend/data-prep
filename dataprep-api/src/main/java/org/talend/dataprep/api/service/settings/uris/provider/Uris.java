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
public interface Uris {

    UriSettings LOGOUT_URI =
            UriSettings.builder()
                    .name("logout")
                    .uri("/logout")
                            .build();

    UriSettings API_USER_URI =
            UriSettings.builder()
                    .name("apiUser")
                    .uri("/api/user")
                    .build();

    UriSettings API_SHARE_URI =
            UriSettings.builder()
                    .name("apiShare")
                    .uri("/api/share")
                    .build();

    UriSettings GROUPS_URI =
            UriSettings.builder()
                    .name("groups")
                    .uri("/groups")
                    .build();

    UriSettings USERS_URI =
            UriSettings.builder()
                    .name("users")
                    .uri("/users")
                    .build();

    UriSettings API_EXPORT_ASYNC_URI =
            UriSettings.builder()
                    .name("apiExportAsync")
                    .uri("/api/export/async")
                    .build();

    UriSettings API_URI =
            UriSettings.builder()
                    .name("api")
                    .uri("/api")
                    .build();

    UriSettings API_AGGREGATE_URI =
            UriSettings.builder()
                    .name("apiAggregate")
                    .uri("/api/aggregate")
                    .build();

    UriSettings API_DATASETS_URI =
            UriSettings.builder()
                    .name("apiDatasets")
                    .uri("/api/datasets")
                    .build();

    UriSettings API_EXPORT_URI =
            UriSettings.builder()
                    .name("apiExport")
                    .uri("/api/export")
                    .build();

    UriSettings API_FOLDERS_URI =
            UriSettings.builder()
                    .name("apiFolders")
                    .uri("/api/folders")
                    .build();

    UriSettings API_MAIL_URI =
            UriSettings.builder()
                    .name("apiMail")
                    .uri("/api/mail")
                    .build();

    UriSettings API_PREPARATIONS_URI =
            UriSettings.builder()
                    .name("apiPreparations")
                    .uri("/api/preparations")
                    .build();

    UriSettings API_PREPARATIONS_PREVIEW_URI =
            UriSettings.builder()
                    .name("apiPreparationsPreview")
                    .uri("/api/preparations/preview")
                    .build();

    UriSettings API_SEARCH_URI =
            UriSettings.builder()
                    .name("apiSearch")
                    .uri("/api/search")
                    .build();

    UriSettings API_SETTINGS_URI =
            UriSettings.builder()
                    .name("apiSettings")
                    .uri("/api/settings")
                    .build();

    UriSettings API_TCOMP_URI =
            UriSettings.builder()
                    .name("apiTcomp")
                    .uri("/api/tcomp")
                    .build();

    UriSettings API_TRANSFORM_URI =
            UriSettings.builder()
                    .name("apiTransform")
                    .uri("/api/transform")
                    .build();

    UriSettings API_TYPES_URI =
            UriSettings.builder()
                    .name("apiTypes")
                    .uri("/api/types")
                    .build();

    UriSettings API_UPGRADE_CHECK_URI =
            UriSettings.builder()
                    .name("apiUpgradeCheck")
                    .uri("/api/upgrade/check")
                    .build();

    UriSettings API_VERSION_URI =
            UriSettings.builder()
                    .name("apiVersion")
                    .uri("/api/version")
                    .build();

    UriSettings LOGIN_URI =
            UriSettings.builder()
                    .name("login")
                    .uri("/login")
                    .build();

    UriSettings LOGOUT_REDIRECTION_URI =
            UriSettings.builder()
                    .name("logoutRedirect")
                    .uri("http://localhost:9080/oidc/idp/logout?client_id=J3Wqyvlzgw0ySg&post_logout_redirect_uri=http://localhost:3000/")
                    .build();
}
// @formatter:on
