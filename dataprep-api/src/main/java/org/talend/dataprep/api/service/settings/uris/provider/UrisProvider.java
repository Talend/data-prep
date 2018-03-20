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

import static java.util.Arrays.asList;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.uris.api.UriSettings;

/**
 * Default views settings provider
 */
@Component
public class UrisProvider implements AppSettingsProvider<UriSettings> {

    @Override
    public List<UriSettings> getSettings() {

        // @formatter:off
        return asList(
                Uris.API_AGGREGATE_URI,
                Uris.API_DATASETS_URI,
                Uris.API_UPLOAD_DATASETS_URI,
                Uris.API_EXPORT_URI,
                Uris.API_FOLDERS_URI,
                Uris.API_MAIL_URI,
                Uris.API_PREPARATIONS_URI,
                Uris.API_PREPARATIONS_PREVIEW_URI,
                Uris.API_SEARCH_URI,
                Uris.API_SETTINGS_URI,
                Uris.API_TCOMP_URI,
                Uris.API_TRANSFORM_URI,
                Uris.API_TYPES_URI,
                Uris.API_UPGRADE_CHECK_URI,
                Uris.API_VERSION_URI
        );
        // @formatter:on
    }

}
