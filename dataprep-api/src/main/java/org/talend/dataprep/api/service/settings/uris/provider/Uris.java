// ============================================================================
//
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

package org.talend.dataprep.api.service.settings.uris.provider;

import org.talend.dataprep.api.service.settings.uris.api.UriSettings;

/**
 * Uris elements configuration
 */

// @formatter:off
public interface Uris {

    UriSettings API_AGGREGATE_URI =
            UriSettings.builder()
                    .id("apiAggregate")
                    .uri("/api/aggregate")
                    .build();

    UriSettings API_DATASETS_URI =
            UriSettings.builder()
                    .id("apiDatasets")
                    .uri("/api/datasets")
                    .build();

    UriSettings API_UPLOAD_DATASETS_URI =
            UriSettings.builder()
                    .id("apiUploadDatasets")
                    .uri("/api/datasets")
                    .build();

    UriSettings API_EXPORT_URI =
            UriSettings.builder()
                    .id("apiExport")
                    .uri("/api/export")
                    .build();

    UriSettings API_FOLDERS_URI =
            UriSettings.builder()
                    .id("apiFolders")
                    .uri("/api/folders")
                    .build();

    UriSettings API_MAIL_URI =
            UriSettings.builder()
                    .id("apiMail")
                    .uri("/api/mail")
                    .build();

    UriSettings API_PREPARATIONS_URI =
            UriSettings.builder()
                    .id("apiPreparations")
                    .uri("/api/preparations")
                    .build();

    UriSettings API_PREPARATIONS_PREVIEW_URI =
            UriSettings.builder()
                    .id("apiPreparationsPreview")
                    .uri("/api/preparations/preview")
                    .build();

    UriSettings API_SEARCH_URI =
            UriSettings.builder()
                    .id("apiSearch")
                    .uri("/api/search")
                    .build();

    UriSettings API_SETTINGS_URI =
            UriSettings.builder()
                    .id("apiSettings")
                    .uri("/api/settings")
                    .build();

    UriSettings API_TCOMP_URI =
            UriSettings.builder()
                    .id("apiTcomp")
                    .uri("/api/tcomp")
                    .build();

    UriSettings API_TRANSFORM_URI =
            UriSettings.builder()
                    .id("apiTransform")
                    .uri("/api/transform")
                    .build();

    UriSettings API_TYPES_URI =
            UriSettings.builder()
                    .id("apiTypes")
                    .uri("/api/types")
                    .build();

    UriSettings API_UPGRADE_CHECK_URI =
            UriSettings.builder()
                    .id("apiUpgradeCheck")
                    .uri("/api/upgrade/check")
                    .build();

    UriSettings API_VERSION_URI =
            UriSettings.builder()
                    .id("apiVersion")
                    .uri("/api/version")
                    .build();

}
// @formatter:on
