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

@TestPropertySource(properties = { "security.oidc.client.endSessionEndpoint=http://localhost:9080/oidc/idp/logout",
        "security.postLogoutRedirect=http://localhost:3000/", "security.oauth2.client.clientId=J3Wqyvlzgw0ySg",
        "security.oauth2.login.uri:/otherLogin", "security.oauth2.logout.uri:/otherLogout" })
public class UrisAppSettingsAPITest extends ApiServiceTestBase {

    @Test
    public void shouldCreateUrisSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final Map<String, String> mapUriSettings = settings.getUris();

        assertThat(mapUriSettings.size(), is(23));

        // then
        assertThat(mapUriSettings.containsKey("apiUser"), is(true));
        assertThat(mapUriSettings.get("apiUser"), is("/api/user"));

        // then
        assertThat(mapUriSettings.containsKey("apiShare"), is(true));
        assertThat(mapUriSettings.get("apiShare"), is("/api/share"));

        // then
        assertThat(mapUriSettings.containsKey("groups"), is(true));
        assertThat(mapUriSettings.get("groups"), is("/groups"));

        // then
        assertThat(mapUriSettings.containsKey("users"), is(true));
        assertThat(mapUriSettings.get("users"), is("/users"));

        // then
        assertThat(mapUriSettings.containsKey("apiExportAsync"), is(true));
        assertThat(mapUriSettings.get("apiExportAsync"), is("/api/export/async"));

        // then
        assertThat(mapUriSettings.containsKey("api"), is(true));
        assertThat(mapUriSettings.get("api"), is("/api"));

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

        // Builded Uri Part
        // then
        assertThat(mapUriSettings.containsKey("logout"), is(true));
        assertThat(mapUriSettings.get("logout"), is("/otherLogout"));

        // then
        assertThat(mapUriSettings.containsKey("logoutRedirect"), is(true));
        assertThat(mapUriSettings.get("logoutRedirect"), is(
                "http://localhost:9080/oidc/idp/logout?client_id=J3Wqyvlzgw0ySg&post_logout_redirect_uri=http://localhost:3000/"));

        // then
        assertThat(mapUriSettings.containsKey("login"), is(true));
        assertThat(mapUriSettings.get("login"), is("/otherLogin"));
    }
}
