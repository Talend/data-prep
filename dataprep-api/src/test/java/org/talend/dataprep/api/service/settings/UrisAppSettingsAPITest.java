// ============================================================================
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

package org.talend.dataprep.api.service.settings;

import static com.jayway.restassured.RestAssured.when;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_ARGS_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.service.ApiServiceTestBase;
import org.talend.dataprep.api.service.settings.actions.api.ActionDropdownSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings;
import org.talend.dataprep.api.service.settings.uris.api.UriSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.list.ListSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

public class UrisAppSettingsAPITest extends ApiServiceTestBase {

    @Test
    public void shouldCreateUrisSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final Map<String, String> mapUriSettings = settings.getUris();

        assertThat(mapUriSettings.size(), is(23));

        assertThat(mapUriSettings.get("logout"), is("/logout"));

    }

}
