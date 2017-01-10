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
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.TYPE_DROPDOWN;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.TYPE_SPLIT_DROPDOWN;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Actions settings are the configuration for a simple button.
 * see https://talend.github.io/react-talend-components/?selectedKind=Action&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "displayMode")
@JsonSubTypes({ @JsonSubTypes.Type(value = ActionDropdownSettings.class, name = TYPE_DROPDOWN),
        @JsonSubTypes.Type(value = ActionSplitDropdownSettings.class, name = TYPE_SPLIT_DROPDOWN) })
public class ActionSettings {

    public static final String TYPE_DROPDOWN = "dropdown";

    public static final String TYPE_SPLIT_DROPDOWN = "splitDropdown";

    public static final String PAYLOAD_METHOD_KEY = "method";

    public static final String PAYLOAD_ARGS_KEY = "args";

    /**
     * The key that will hold this settings in the actions dictionary
     */
    private String id;

    /**
     * The action display name
     */
    private String name;

    /**
     * The action icon that will appears next to the name
     */
    private String icon;

    /**
     * The type of action (can be the same as other actions if they triggers the same code behind the scene). An action can have
     * the same code but a different name for example.
     */
    private String type;

    /**
     * Is the action a link (no button style)
     */
    private Boolean link;

    /**
     * Does the label should be hidden (icon only). The label will be in the tooltip
     */
    private Boolean hideLabel;

    /**
     * The bootstrap style (ex: primary to apply the primary color)
     */
    private String bsStyle;

    /**
     * The bootstrap size (ex: small to render a small button)
     */
    private String bsSize;

    /**
     * Can hold any extra static information to pass to the action function
     */
    private Map<String, Object> payload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getHideLabel() {
        return hideLabel;
    }

    public void setHideLabel(Boolean hideLabel) {
        this.hideLabel = hideLabel;
    }

    public Boolean getLink() {
        return link;
    }

    public void setLink(Boolean link) {
        this.link = link;
    }

    public String getBsStyle() {
        return bsStyle;
    }

    public void setBsStyle(String bsStyle) {
        this.bsStyle = bsStyle;
    }

    public String getBsSize() {
        return bsSize;
    }

    public void setBsSize(String bsSize) {
        this.bsSize = bsSize;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public static Builder from(final ActionSettings actionSettings) {
        return builder().id(actionSettings.getId()).name(actionSettings.getName()).icon(actionSettings.getIcon())
                .type(actionSettings.getType()).bsStyle(actionSettings.getBsStyle()).payload(actionSettings.getPayload());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;

        private String name;

        private String icon;

        private String type;

        private Boolean link;

        private Boolean hideLabel;

        private String bsStyle;

        private String bsSize;

        private Map<String, Object> payload = new HashMap<>();

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

        public Builder isLink() {
            this.link = true;
            return this;
        }

        public Builder hideLabel() {
            this.hideLabel = true;
            return this;
        }

        public Builder bsStyle(final String bsStyle) {
            this.bsStyle = bsStyle;
            return this;
        }

        public Builder bsSize(final String bsSize) {
            this.bsSize = bsSize;
            return this;
        }

        public Builder payload(final String key, final Object value) {
            this.payload.put(key, value);
            return this;
        }

        public Builder payload(final Map<String, Object> payload) {
            payload.entrySet().stream().forEach(entry -> this.payload.put(entry.getKey(), entry.getValue()));
            return this;
        }

        public ActionSettings build() {
            final ActionSettings action = new ActionSettings();
            action.setId(this.id);
            action.setName(this.name);
            action.setIcon(this.icon);
            action.setType(this.type);
            action.setLink(this.link);
            action.setHideLabel(this.hideLabel);
            action.setBsStyle(this.bsStyle);
            action.setBsSize(this.bsSize);
            action.setPayload(this.payload.isEmpty() ? null : this.payload);
            return action;
        }
    }
}
