//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.settings.actions.provider;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

public interface SearchActions {

    ActionSettings SEARCH_TOGGLE = ActionSettings.builder()
            .id("search:toggle")
            .name("Toggle search input")
            .icon("talend-search")
            .type("@@search/TOGGLE")
            .build();

    ActionSettings SEARCH_ALL = ActionSettings.builder()
            .id("search:all")
            .type("@@search/ALL")
            .build();

    ActionSettings SEARCH_FOCUS = ActionSettings.builder()
            .id("search:focus")
            .type("@@search/FOCUS")
            .build();
}
