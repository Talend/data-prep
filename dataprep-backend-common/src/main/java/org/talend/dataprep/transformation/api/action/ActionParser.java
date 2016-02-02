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

package org.talend.dataprep.transformation.api.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Actions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Parse the actions a dataset and prepare the closures to apply.
 */
@Component
public class ActionParser {

    /** No op parsed actions. */
    //@formatter:off
    public static final ParsedActions IDLE_CONSUMER = new ParsedActions(Action.IDLE_ROW_ACTION);
    //@formatter:on

    /** The dataprep ready jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    /** The dataprep spring application context. */
    @Autowired
    private ApplicationContext context;

    /**
     * Return the parsed actions ready to be run.
     *
     * @param actions the actions to be parsed as string.
     * @return the parsed actions.
     */
    public ParsedActions parse(String actions) {
        if (actions == null) {
            // Actions cannot be null (but can be empty string for no op actions).
            throw new IllegalArgumentException("Actions parameter can not be null.");
        }
        try {
            if (StringUtils.isEmpty(actions)) {
                return IDLE_CONSUMER;
            }
            // Parse action JSON
            final Actions parsedActions = builder.build().readerFor(Actions.class).readValue(actions);
            // Create closures from parsed actions
            List<DataSetRowAction> rowActions = new ArrayList<>();
            final List<Action> allActions = parsedActions.getActions();
            for (Action parsedAction : allActions) {
                final String name = ActionMetadata.ACTION_BEAN_PREFIX + parsedAction.getAction().toLowerCase();
                final ActionMetadata metadata = context.getBean(name, ActionMetadata.class);
                final Action action = metadata.create(parsedAction.getParameters());
                rowActions.add(action.getRowAction());
            }
            // all set: wraps everything and return to caller
            return new ParsedActions(allActions, rowActions);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }
}
