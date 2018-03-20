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

package org.talend.dataprep.api.service.settings.views.api.sidepanel;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.i18n.DataprepBundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Side panel settings
 * see
 * https://talend.github.io/react-talend-components/?selectedKind=Side%20Panel&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
public class SidePanelSettings implements ViewSettings {

    public static final String VIEW_TYPE = TYPE_SIDE_PANEL;

    /**
     * The key in the views dictionary
     */
    @JsonIgnore
    private String id;

    /**
     * The panel toggle action identifier
     */
    private String onToggleDock;

    /**
     * The list of actions that represent the items in the menu
     */
    private List<String> actions;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ViewSettings translate() {
        return SidePanelSettings
                .from(this) //
                .translate() //
                .build();
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

    public static Builder from(final SidePanelSettings viewSettings) {
        return builder() //
                .id(viewSettings.getId()) //
                .onToggleDock(viewSettings.getOnToggleDock()) //
                .actions(viewSettings.getActions());
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

        public Builder actions(final List<String> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public SidePanelSettings build() {
            final SidePanelSettings settings = new SidePanelSettings();
            settings.setId(this.id);
            settings.setOnToggleDock(this.onToggleDock);
            settings.setActions(this.actions);
            return settings;
        }

        public Builder translate() {
            if (Objects.nonNull(this.actions)) {
                this.actions = this.actions
                        .stream() //
                        .map(entry -> DataprepBundle.message(entry)) //
                        .collect(Collectors.toList());
            }
            return this;
        }
    }
}
