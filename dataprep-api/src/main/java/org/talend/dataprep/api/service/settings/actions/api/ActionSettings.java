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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.list.ListSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "displayMode"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDropdownSettings.class, name = "dropdown"),
        @JsonSubTypes.Type(value = ActionSplitDropdownSettings.class, name = "splitDropdown")
})
public class ActionSettings {
    public static final String PAYLOAD_METHOD_KEY = "method";
    public static final String PAYLOAD_ARGS_KEY = "args";

    private String id;
    private String name;
    private String icon;
    private String type;
    private String bsStyle;
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

    public String getBsStyle() {
        return bsStyle;
    }

    public void setBsStyle(String bsStyle) {
        this.bsStyle = bsStyle;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public static Builder from(final ActionSettings actionSettings) {
        return builder()
                .id(actionSettings.getId())
                .name(actionSettings.getName())
                .icon(actionSettings.getIcon())
                .type(actionSettings.getType())
                .bsStyle(actionSettings.getBsStyle())
                .payload(actionSettings.getPayload());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String icon;
        private String type;
        private String bsStyle;
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

        public Builder bsStyle(final String bsStyle) {
            this.bsStyle = bsStyle;
            return this;
        }

        public Builder payload(final String key, final Object value) {
            this.payload.put(key, value);
            return this;
        }

        public Builder payload(final Map<String, Object> payload) {
            payload.entrySet()
                    .stream()
                    .forEach(entry -> this.payload.put(entry.getKey(), entry.getValue()));
            return this;
        }

        public ActionSettings build() {
            final ActionSettings action = new ActionSettings();
            action.setId(this.id);
            action.setName(this.name);
            action.setIcon(this.icon);
            action.setType(this.type);
            action.setBsStyle(this.bsStyle);
            action.setPayload(this.payload.isEmpty() ? null : this.payload);
            return action;
        }
    }
}
