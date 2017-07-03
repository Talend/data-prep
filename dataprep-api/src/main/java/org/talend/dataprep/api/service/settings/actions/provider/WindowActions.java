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

package org.talend.dataprep.api.service.settings.actions.provider;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_ARGS_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings.splitDropdownBuilder;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
/**
 * Actions that triggers windows (modal, new tab, ...) settings
 */
// @formatter:off
public interface WindowActions {
    ActionSettings ONBOARDING_PREPARATION = builder()
            .id("onboarding:preparation")
            .name("Guided tour")
            .icon("talend-board")
            .type("@@onboarding/START_TOUR")
            .payload(PAYLOAD_METHOD_KEY, "startTour")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"preparation"})
            .build();

    ActionSettings ONBOARDING_PLAYGROUND = builder()
            .id("onboarding:playground")
            .name("Guided tour")
            .icon("talend-board")
            .type("@@onboarding/START_TOUR")
            .payload(PAYLOAD_METHOD_KEY, "startTour")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"playground"})
            .build();

    ActionSettings MODAL_ABOUT = builder()
            .id("modal:about")
            .name("About Data Preparation")
            .icon("talend-info-circle")
            .type("@@modal/SHOW")
            .payload(PAYLOAD_METHOD_KEY, "toggleAbout")
            .build();

    ActionSettings MODAL_FEEDBACK = builder()
            .id("modal:feedback")
            .name("Feedback")
            .icon("talend-bubbles")
            .type("@@modal/SHOW")
            .payload(PAYLOAD_METHOD_KEY, "showFeedback")
            .build();

    ActionSettings EXTERNAL_HELP = builder()
            .id("external:help")
            .name("Help")
            .icon("talend-question-circle")
            .type("@@external/OPEN_WINDOW")
            .payload(PAYLOAD_METHOD_KEY, "open")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"https://help.talend.com/#/search/all?filters=EnrichPlatform%253D%2522Talend+Data+Preparation%2522%2526EnrichVersion%253D%25222.1%2522&utm_medium=dpdesktop&utm_source=header"})
            .build();

    ActionSettings HEADERBAR_HELP = splitDropdownBuilder()
            .id("headerbar:help")
            .name("Help")
            .icon("talend-question-circle")
            .type("@@headerbar/HELP")
            .action("external:help")
            .build();

    ActionSettings PLAYGROUND_HEADERBAR_HELP = splitDropdownBuilder()
            .id("playground:headerbar:help")
            .name("Help")
            .icon("talend-question-circle")
            .type("@@headerbar/HELP")
            .action("external:help")
            .build();

    ActionSettings EXTERNAL_DOCUMENTATION = builder()
            .id("external:documentation")
            .name("Documentation")
            .icon("talend-question-circle")
            .type("@@external/OPEN_WINDOW")
            .payload(PAYLOAD_METHOD_KEY, "open")
            .build();
}
// @formatter:on
