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

package org.talend.dataprep.api.service.settings.actions.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsConfigurer;
import org.talend.dataprep.api.service.settings.actions.api.ActionDropdownSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.provider.ExternalHelpActionsProvider;

import java.util.Arrays;

import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.*;

/**
 * Settings configurer that insert the actions as the PLAYGROUND_HEADERBAR_INFORMATION dropdown items.
 */
@Component
public class HeaderbarInformationPlaygroundConfigurer extends AppSettingsConfigurer<ActionSettings> {

    @Autowired
    private ExternalHelpActionsProvider externalHelpActionsProvider;

    @Override
    public boolean isApplicable(final ActionSettings actionSettings) {
        return actionSettings == HEADERBAR_INFORMATION_PLAYGROUND;
    }

    @Override
    public ActionSettings configure(final ActionSettings actionSettings) {
        return ActionDropdownSettings
                .from((ActionDropdownSettings) actionSettings) //
                .staticActions(Arrays.asList( //
                        MODAL_ABOUT.getId(), //
                        ONBOARDING_PLAYGROUND.getId(), //
                        DIVIDER.getId(), //
                        externalHelpActionsProvider.getExternalCommunityAction().getId(), //
                        MODAL_FEEDBACK.getId() //
                )) //
                .build();
    }

}
