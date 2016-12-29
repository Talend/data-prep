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

package org.talend.dataprep.api.service.settings.views.api.sidepanel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class SidePanelSettings implements ViewSettings {
    public static final String VIEW_TYPE = "SidePanel";

    @JsonIgnore
    private String id;
    private String onToggleDock;
    private List<String> actions;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOnToggleDock() {
        return onToggleDock;
    }

    public void setOnToggleDock(String onToggleDock) {
        this.onToggleDock = onToggleDock;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String onToggleDock;
        private List<String> actions = new ArrayList<>();

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder onToggleDock(final String onToggleDock) {
            this.onToggleDock = onToggleDock;
            return this;
        }

        public Builder action(final String action) {
            this.actions.add(action);
            return this;
        }

        public SidePanelSettings build() {
            final SidePanelSettings settings = new SidePanelSettings();
            settings.setId(this.id);
            settings.setOnToggleDock(this.onToggleDock);
            settings.setActions(this.actions);
            return settings;
        }

    }
}
