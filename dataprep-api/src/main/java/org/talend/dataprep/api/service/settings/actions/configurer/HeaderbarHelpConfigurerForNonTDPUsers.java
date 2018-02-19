package org.talend.dataprep.api.service.settings.actions.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.service.settings.AppSettingsConfigurer;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings;
import org.talend.dataprep.api.service.settings.actions.provider.ExternalHelpActionsProvider;

import java.util.Arrays;

import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.*;

public class HeaderbarHelpConfigurerForNonTDPUsers extends AppSettingsConfigurer<ActionSettings> {

    @Autowired
    private ExternalHelpActionsProvider externalHelpActionsProvider;

    @Override
    public boolean isApplicable(final ActionSettings actionSettings) {
        return actionSettings == HEADERBAR_HELP_FOR_NON_TDP_USERS;
    }

    @Override
    public ActionSettings configure(final ActionSettings actionSettings) {
        return ActionSplitDropdownSettings
                .from((ActionSplitDropdownSettings) actionSettings) //
                .items(Arrays.asList( //
                        externalHelpActionsProvider.getExternalHelpAction().getId(), //
                        externalHelpActionsProvider.getExternalCommunityAction().getId(), //
                        MODAL_ABOUT.getId())) //
                .build();
    }

}
