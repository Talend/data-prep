/*
 * ============================================================================
 *
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

package org.talend.dataprep.api.service.settings.actions.provider;

import static java.util.Arrays.asList;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

@Component
public class DatasetActionsProvider implements AppSettingsProvider<ActionSettings> {

    @Override
    public List<ActionSettings> getSettings() {
        // @formatter:off
        return asList(
                DatasetActions.DATASET_CLONE,
                DatasetActions.DATASET_CREATE,
                DatasetActions.DATASET_DISPLAY_MODE,
                DatasetActions.DATASET_FAVORITE,
                DatasetActions.DATASET_FETCH,
                DatasetActions.DATASET_OPEN,
                DatasetActions.DATASET_PREPARATIONS,
                DatasetActions.DATASET_REMOVE,
                DatasetActions.DATASET_SORT,
                DatasetActions.DATASET_SUBMIT_EDIT,
                DatasetActions.DATASET_UPDATE
        );
        // @formatter:on
    }
}
