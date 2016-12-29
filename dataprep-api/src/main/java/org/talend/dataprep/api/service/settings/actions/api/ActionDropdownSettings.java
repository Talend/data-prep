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

package org.talend.dataprep.api.service.settings.actions.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ActionDropdownSettings extends ActionSettings {
    private final String displayMode = "dropdown";
    private String items;
    private String dynamicAction;
    private List<String> staticActions;

    public String getDisplayMode() {
        return displayMode;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getDynamicAction() {
        return dynamicAction;
    }

    public void setDynamicAction(String dynamicAction) {
        this.dynamicAction = dynamicAction;
    }

    public List<String> getStaticActions() {
        return staticActions;
    }

    public void setStaticActions(List<String> staticActions) {
        this.staticActions = staticActions;
    }

    public static Builder from(final ActionDropdownSettings actionSettings) {
        return dropdownBuilder()
                .id(actionSettings.getId())
                .name(actionSettings.getName())
                .icon(actionSettings.getIcon())
                .bsStyle(actionSettings.getBsStyle())
                .items(actionSettings.getItems())
                .dynamicAction(actionSettings.getDynamicAction())
                .staticActions(actionSettings.getStaticActions());
    }

    public static Builder dropdownBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String icon;
        private String bsStyle;
        private String items;
        private String dynamicAction;
        private List<String> staticActions = new ArrayList<>();

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

        public Builder bsStyle(final String bsStyle) {
            this.bsStyle = bsStyle;
            return this;
        }

        public Builder items(final String items) {
            this.items = items;
            return this;
        }

        public Builder dynamicAction(final String dynamicAction) {
            this.dynamicAction = dynamicAction;
            return this;
        }

        public Builder staticAction(final String staticAction) {
            this.staticActions.add(staticAction);
            return this;
        }

        public Builder staticActions(final List<String> staticActions) {
            this.staticActions.addAll(staticActions);
            return this;
        }

        public ActionDropdownSettings build() {
            final ActionDropdownSettings action = new ActionDropdownSettings();
            action.setId(this.id);
            action.setName(this.name);
            action.setIcon(this.icon);
            action.setBsStyle(this.bsStyle);
            action.setItems(this.items);
            action.setDynamicAction(dynamicAction);
            action.setStaticActions(staticActions.isEmpty() ? null : staticActions);
            return action;
        }
    }
}
