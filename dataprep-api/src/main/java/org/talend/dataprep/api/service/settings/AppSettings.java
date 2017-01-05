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

package org.talend.dataprep.api.service.settings;

import java.util.HashMap;
import java.util.Map;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

/**
 * Application settings.
 * This contains the views and the actions configurations.
 */
public class AppSettings {

    /**
     * The views settings dictionary
     */
    private final Map<String, ViewSettings> views = new HashMap<>();

    /**
     * The actions settings dictionary
     */
    private final Map<String, ActionSettings> actions = new HashMap<>();

    public Map<String, ViewSettings> getViews() {
        return views;
    }

    public Map<String, ActionSettings> getActions() {
        return actions;
    }
}
