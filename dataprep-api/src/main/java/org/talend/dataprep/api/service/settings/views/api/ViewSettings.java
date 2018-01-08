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

package org.talend.dataprep.api.service.settings.views.api;

import static org.talend.dataprep.api.service.settings.views.api.ViewSettings.*;

import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.list.ListSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Settings that configure a view
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "VIEW_TYPE")
@JsonSubTypes({ @JsonSubTypes.Type(value = AppHeaderBarSettings.class, name = TYPE_APP_HEADER_BAR),
        @JsonSubTypes.Type(value = BreadcrumbSettings.class, name = TYPE_BREADCRUMB),
        @JsonSubTypes.Type(value = ListSettings.class, name = TYPE_LIST),
        @JsonSubTypes.Type(value = SidePanelSettings.class, name = TYPE_SIDE_PANEL) })
public interface ViewSettings {

    String TYPE_APP_HEADER_BAR = "AppHeaderBar";

    String TYPE_BREADCRUMB = "Breadcrumb";

    String TYPE_LIST = "List";

    String TYPE_SIDE_PANEL = "SidePanel";

    String getId();

    ViewSettings translate();

}
