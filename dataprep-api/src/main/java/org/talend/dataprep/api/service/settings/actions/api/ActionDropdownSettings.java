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

package org.talend.dataprep.api.service.settings.actions.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.DataprepBundle;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Action dropdown settings are the configuration to display a dropdown.
 * Keep in mind that a dropdown action is always applied to an object (called the model).
 * A dropdown can have several statics actions and/or a dynamic action.
 * see
 * https://talend.github.io/react-talend-components/?selectedKind=ActionDropdown&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 *
 * Static actions
 * They represents dropdown options displayed on top of the options, and triggers a simple action on the model.
 *
 * Dynamic action
 * The action that will be applied to the items (that will replace the model). They must be used in pair (if there are items,
 * there is a dynamic action). The items String value is the property key to get the items objects from the model.
 *
 */
@JsonInclude(NON_NULL)
public class ActionDropdownSettings extends ActionSettings {

    private final String displayMode = TYPE_DROPDOWN;

    /**
     * The property key to get the items objects
     */
    private String items;

    /**
     * The action id that will be applied to the items
     */
    private String dynamicAction;

    /**
     * The statics actions ids that will be applied to the hosting model
     */
    private List<String> staticActions;

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

    @Override
    public ActionSettings translate() {
        return ActionDropdownSettings //
                .from(this) //
                .translate() //
                .build();
    }

    public static Builder from(final ActionDropdownSettings actionSettings) {
        return dropdownBuilder() //
                .id(actionSettings.getId()) //
                .name(actionSettings.getName()) //
                .icon(actionSettings.getIcon()) //
                .bsStyle(actionSettings.getBsStyle()) //
                .items(actionSettings.getItems()) //
                .dynamicAction(actionSettings.getDynamicAction()) //
                .staticActions(actionSettings.getStaticActions()) //
                .enabled(actionSettings.isEnabled());
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

        private boolean enabled = true;

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

        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
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
            if (staticActions != null) {
                this.staticActions.addAll(staticActions);
            }
            return this;
        }

        public ActionDropdownSettings.Builder translate() {
            if (StringUtils.isNotEmpty(this.name)) {
                this.name = DataprepBundle.message(this.name);
            }
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
            action.setEnabled(this.enabled);
            return action;
        }
    }
}
