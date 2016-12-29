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

package org.talend.dataprep.api.service.settings.views.api.actionsbar;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ActionsBarSettings {
    private Map<String, List<String>> actions;

    public Map<String, List<String>> getActions() {
        return actions;
    }

    public void setActions(final Map<String, List<String>> actions) {
        this.actions = actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, List<String>> actions = new HashMap<>();

        public Builder action(final ActionsPosition position, final String action) {
            List<String> positionActions = actions.get(position.getValue());
            if(positionActions == null) {
                positionActions = new ArrayList<>();
                actions.put(position.getValue(), positionActions);
            }
            positionActions.add(action);
            return this;
        }

        public ActionsBarSettings build() {
            final ActionsBarSettings settings = new ActionsBarSettings();
            settings.setActions(this.actions);
            return settings;
        }

    }

    public enum ActionsPosition {
        LEFT("left"), RIGHT("right");

        private final String value;
        ActionsPosition(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
