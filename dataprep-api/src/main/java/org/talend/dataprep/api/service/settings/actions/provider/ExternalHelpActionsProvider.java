package org.talend.dataprep.api.service.settings.actions.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.help.DocumentationLinksManager;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.*;

@Component
public class ExternalHelpActionsProvider {

    @Autowired
    private DocumentationLinksManager documentationLinksManager;

    public ActionSettings getExternalHelpAction() {
        return builder()
                .id("external:help")
                .name("Help")
                .icon("talend-question-circle")
                .type("@@external/OPEN_WINDOW")
                .payload(PAYLOAD_METHOD_KEY, "open")
                .payload(PAYLOAD_ARGS_KEY, new String[]{documentationLinksManager.getFuzzyUrl() + "header"})
                .build();
    }
}
