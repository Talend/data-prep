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

package org.talend.dataprep.api.service.settings.actions.provider;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Search actions settings
 */
// @formatter:off
public interface SearchActions {
    ActionSettings SEARCH_TOGGLE = builder()
            .id("search:toggle")
            .name("search.toggle")
            .icon("talend-search")
            .type("@@search/TOGGLE")
            .build();

    ActionSettings SEARCH_ALL = builder()
            .id("search:all")
            .type("@@search/ALL")
            .build();

    ActionSettings SEARCH_DOC = builder()
            .id("search:doc")
            .type("@@search/DOC")
            .build();

    ActionSettings SEARCH_FOCUS = builder()
            .id("search:focus")
            .type("@@search/FOCUS")
            .build();
}
// @formatter:on
