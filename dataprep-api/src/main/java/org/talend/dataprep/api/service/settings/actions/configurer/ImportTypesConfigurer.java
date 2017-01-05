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

package org.talend.dataprep.api.service.settings.actions.configurer;

import static java.util.Collections.emptyList;
import static org.talend.dataprep.api.service.settings.actions.provider.DatasetActions.DATASET_CREATE;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.Import;
import org.talend.dataprep.api.service.command.dataset.DataSetGetImports;
import org.talend.dataprep.api.service.settings.AppSettingsConfigurer;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings;
import org.talend.dataprep.exception.TDPException;

/**
 * Settings configurer that insert the imports types as the DATASET_CREATE split dropdown items.
 */
@Component
public class ImportTypesConfigurer extends AppSettingsConfigurer<ActionSettings> {

    @Override
    public boolean isApplicable(final ActionSettings actionSettings) {
        return actionSettings == DATASET_CREATE;
    }

    @Override
    public ActionSettings configure(final ActionSettings actionSettings) {
        return ActionSplitDropdownSettings.from((ActionSplitDropdownSettings) actionSettings).items(getImportTypes()).build();
    }

    private List<Import> getImportTypes() {
        try {
            final DataSetGetImports command = getCommand(DataSetGetImports.class);
            return command.execute();
        } catch (final TDPException e) {
            LOGGER.error("Unable to get import types", e);
            return emptyList();
        }
    }
}
