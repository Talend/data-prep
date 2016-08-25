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

package org.talend.dataprep.transformation.api.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Actions;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parse the actions a dataset and prepare the closures to apply.
 */
public class ActionParser {

    private final ActionFactory factory;

    private final ActionRegistry actionRegistry;

    private final ObjectMapper mapper;

    public ActionParser(ActionFactory factory, ActionRegistry actionRegistry, ObjectMapper mapper) {
        this.factory = factory;
        this.actionRegistry = actionRegistry;
        this.mapper = mapper;
    }

    /**
     * Return the parsed actions ready to be run.
     *
     * @param actions the actions to be parsed as string.
     * @return the parsed actions.
     */
    public List<Action> parse(String actions) {
        if (actions == null) {
            // Actions cannot be null (but can be empty string for no op actions).
            throw new IllegalArgumentException("Actions parameter can not be null.");
        }
        if (StringUtils.isEmpty(actions)) {
            return Collections.emptyList();
        }
        try {
            // Parse action JSON
            final Actions parsedActions = mapper.reader(Actions.class).readValue(actions);
            // Create closures from parsed actions
            final List<Action> allActions = parsedActions.getActions();
            final List<Action> builtActions = new ArrayList<>(allActions.size() + 1);
            parsedActions.getActions().stream() //
                    .filter(parsedAction -> parsedAction != null && parsedAction.getName() != null) //
                    .forEach(parsedAction -> {
                        String actionNameLowerCase = parsedAction.getName().toLowerCase();
                        final ActionDefinition metadata = actionRegistry.get(actionNameLowerCase);
                        builtActions.add(factory.create(metadata, parsedAction.getParameters()));
                    });
            // all set: wraps everything and return to caller
            return builtActions;
        } catch (TalendRuntimeException tpe) {
            // leave TDPException as is
            throw tpe;
        } catch (Exception e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }
}
