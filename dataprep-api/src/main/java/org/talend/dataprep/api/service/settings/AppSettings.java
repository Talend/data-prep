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

    /** The views settings dictionary. */
    private final Map<String, ViewSettings> views = new HashMap<>();

    /** The actions settings dictionary. */
    private final Map<String, ActionSettings> actions = new HashMap<>();

    /** The actions settings dictionary. */
    private final Map<String, String> uris = new HashMap<>();

    /** The help settings dictionary. */
    private final Map<String, String> help = new HashMap<>();

    /** The analytics settings dictionary. */
    private final Map<String, String> analytics = new HashMap<>();
    /**
     * The context settings dictionary
     */
    private final Map<String, String> context = new HashMap<>();

    /**
     * Getters
     */
    public Map<String, ViewSettings> getViews() {
        return views;
    }

    public Map<String, ActionSettings> getActions() {
        return actions;
    }

    public Map<String, String> getUris() {
        return uris;
    }

    public Map<String, String> getHelp() {
        return help;
    }

    public Map<String, String> getAnalytics() {
        return analytics;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
