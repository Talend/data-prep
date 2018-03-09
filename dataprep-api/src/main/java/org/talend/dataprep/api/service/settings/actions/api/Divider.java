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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.DataprepBundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.TYPE_DROPDOWN;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.TYPE_SPLIT_DROPDOWN;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "displayMode")
@JsonSubTypes({@JsonSubTypes.Type(value = ActionDropdownSettings.class, name = TYPE_DROPDOWN),
        @JsonSubTypes.Type(value = ActionSplitDropdownSettings.class, name = TYPE_SPLIT_DROPDOWN)})
public class Divider implements SettingElement {
    public boolean isEnabled() {
        return true;
    }

    public Divider translate() { return this; }

    public String getId() {
        return "divider";
    }
}
