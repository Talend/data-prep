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

package org.talend.dataprep.api.service.settings.views.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.list.ListSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "VIEW_TYPE"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AppHeaderBarSettings.class, name = "AppHeaderBar"),
        @JsonSubTypes.Type(value = BreadcrumbSettings.class, name = "Breadcrumb"),
        @JsonSubTypes.Type(value = ListSettings.class, name = "List"),
        @JsonSubTypes.Type(value = SidePanelSettings.class, name = "SidePanel")
})
public interface ViewSettings {
    String getId();
}
