// ============================================================================
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

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsConfigurer;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings;

import java.util.Arrays;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.*;

/**
 * Settings configurer that insert the actions as the HEADERBAR_HELP split dropdown items.
 */
@Component
public class HeaderbarHelpConfigurer extends AppSettingsConfigurer<ActionSettings> {

    @Override
    public boolean isApplicable(final ActionSettings actionSettings) {
        return actionSettings == HEADERBAR_HELP;
    }

    @Override
    public ActionSettings configure(final ActionSettings actionSettings) {
        return ActionSplitDropdownSettings.from((ActionSplitDropdownSettings) actionSettings) //
                .items(Arrays.asList(EXTERNAL_HELP.getId(), ONBOARDING_PREPARATION.getId(), MODAL_ABOUT.getId(), MODAL_FEEDBACK.getId())) //
                .build();
    }
}
