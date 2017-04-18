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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.service.ApiServiceTestBase;

public class UrisAppSettingsAPITest extends ApiServiceTestBase {

    @Test
    public void shouldCreateUrisSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final Map<String, String> mapUriSettings = settings.getUris();

        assertThat(mapUriSettings.size(), is(14));

        // then
        assertThat(mapUriSettings.containsKey("apiAggregate"), is(true));
        assertThat(mapUriSettings.get("apiAggregate"), is("/api/aggregate"));

        // then
        assertThat(mapUriSettings.containsKey("apiDatasets"), is(true));
        assertThat(mapUriSettings.get("apiDatasets"), is("/api/datasets"));

        // then
        assertThat(mapUriSettings.containsKey("apiExport"), is(true));
        assertThat(mapUriSettings.get("apiExport"), is("/api/export"));

        // then
        assertThat(mapUriSettings.containsKey("apiFolders"), is(true));
        assertThat(mapUriSettings.get("apiFolders"), is("/api/folders"));

        // then
        assertThat(mapUriSettings.containsKey("apiMail"), is(true));
        assertThat(mapUriSettings.get("apiMail"), is("/api/mail"));

        // then
        assertThat(mapUriSettings.containsKey("apiPreparations"), is(true));
        assertThat(mapUriSettings.get("apiPreparations"), is("/api/preparations"));

        // then
        assertThat(mapUriSettings.containsKey("apiPreparationsPreview"), is(true));
        assertThat(mapUriSettings.get("apiPreparationsPreview"), is("/api/preparations/preview"));

        // then
        assertThat(mapUriSettings.containsKey("apiSearch"), is(true));
        assertThat(mapUriSettings.get("apiSearch"), is("/api/search"));

        // then
        assertThat(mapUriSettings.containsKey("apiSettings"), is(true));
        assertThat(mapUriSettings.get("apiSettings"), is("/api/settings"));

        // then
        assertThat(mapUriSettings.containsKey("apiTcomp"), is(true));
        assertThat(mapUriSettings.get("apiTcomp"), is("/api/tcomp"));

        // then
        assertThat(mapUriSettings.containsKey("apiTransform"), is(true));
        assertThat(mapUriSettings.get("apiTransform"), is("/api/transform"));

        // then
        assertThat(mapUriSettings.containsKey("apiTypes"), is(true));
        assertThat(mapUriSettings.get("apiTypes"), is("/api/types"));

        // then
        assertThat(mapUriSettings.containsKey("apiUpgradeCheck"), is(true));
        assertThat(mapUriSettings.get("apiUpgradeCheck"), is("/api/upgrade/check"));

        // then
        assertThat(mapUriSettings.containsKey("apiVersion"), is(true));
        assertThat(mapUriSettings.get("apiVersion"), is("/api/version"));
    }
}
