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

package org.talend.dataprep.api.service.settings.actions.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An action split dropdown is a mix of simple button and a dropdown.
 * see https://talend.github.io/react-talend-components/?selectedKind=ActionSplitDropdown&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 * 
 * Button action
 * This part is configured with the ActionSettings properties. The whole items list will be passed to the action function.
 * 
 * Dropdown
 * The dropdown options are the items. They are the models the actions applies to. The action configuration is the one from simple
 * button configuration.
 */
@JsonInclude(NON_NULL)
public class ActionSplitDropdownSettings extends ActionSettings {

    private final String displayMode = TYPE_SPLIT_DROPDOWN;

    /**
     * The list of items that represents the dropdown options
     */
    private List<Object> items;

    public List<Object> getItems() {
        return items;
    }

    public void setItems(final List<Object> items) {
        this.items = items;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public static Builder from(final ActionSplitDropdownSettings actionSettings) {
        return splitDropdownBuilder().id(actionSettings.getId()).name(actionSettings.getName()).icon(actionSettings.getIcon())
                .type(actionSettings.getType()).bsStyle(actionSettings.getBsStyle()).items(actionSettings.getItems());
    }

    public static Builder splitDropdownBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String id;

        private String name;

        private String icon;

        private String type;

        private String bsStyle;

        private List<Object> items = new ArrayList<>();

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder icon(final String icon) {
            this.icon = icon;
            return this;
        }

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder bsStyle(final String bsStyle) {
            this.bsStyle = bsStyle;
            return this;
        }

        public Builder items(final List<? extends Object> items) {
            this.items.addAll(items);
            return this;
        }

        public ActionSplitDropdownSettings build() {
            final ActionSplitDropdownSettings action = new ActionSplitDropdownSettings();
            action.setId(this.id);
            action.setName(this.name);
            action.setIcon(this.icon);
            action.setType(this.type);
            action.setBsStyle(this.bsStyle);
            action.setItems(this.items);
            return action;
        }
    }
}
