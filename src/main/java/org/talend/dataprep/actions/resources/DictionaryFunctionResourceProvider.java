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

package org.talend.dataprep.actions.resources;

import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_INVALID;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.actions.RemoteResourceGetter;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.service.Dictionaries;

public class DictionaryFunctionResourceProvider implements FunctionResourceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryFunctionResourceProvider.class);

    private final ActionRegistry actionRegistry;

    private final String apiUrl;

    private final String login;

    private final String password;

    public DictionaryFunctionResourceProvider(ActionRegistry actionRegistry, String apiUrl, String login, String password) {
        this.actionRegistry = actionRegistry;
        this.apiUrl = apiUrl;
        this.login = login;
        this.password = password;
    }

    private Dictionaries retrieveDictionaries(List<Action> actions) {
        boolean requireDictionary = false;
        for (Action action : actions) {
            final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
            if (actionDefinition.getBehavior().contains(NEED_STATISTICS_INVALID)) {
                LOGGER.info("Action '{}' requires up to date dictionary.", actionDefinition.getName());
                requireDictionary = true;
                break;
            }
        }

        if (requireDictionary) {
            final RemoteResourceGetter getter = new RemoteResourceGetter();
            LOGGER.info("Retrieving dictionary from Data Prep...");
            final Dictionaries dictionaries = getter.retrieveDictionaries(apiUrl, login, password);
            LOGGER.info("Dictionary retrieved.");
            return dictionaries;
        } else {
            return null;
        }
    }

    @Override
    public FunctionResource get(List<Action> actions) {
        return new DictionaryResource(retrieveDictionaries(actions));
    }
}
