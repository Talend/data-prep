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

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Actions that triggers windows (modal, new tab, ...) settings
 */
// @formatter:off
public interface WindowActions {
    ActionSettings ONBOARDING_PREPARATION = builder()
            .id("onboarding:preparation")
            .name("Click here to discover the application")
            .icon("talend-board")
            .type("@@onboarding/START_TOUR")
            .payload(PAYLOAD_METHOD_KEY, "startTour")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"preparation"})
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
            .name("Send feedback to Talend")
            .icon("talend-bubbles")
            .type("@@modal/SHOW")
            .payload(PAYLOAD_METHOD_KEY, "showFeedback")
            .build();

    ActionSettings EXTERNAL_HELP = builder()
            .id("external:help")
            .name("Open Online Help")
            .icon("talend-question-circle")
            .type("@@external/OPEN_WINDOW")
            .payload(PAYLOAD_METHOD_KEY, "open")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header"})
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
