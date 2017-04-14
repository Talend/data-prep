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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.service.ApiServiceTestBase;
import org.talend.dataprep.api.service.settings.uris.provider.UrisBuilder;

public class UrisBuilderTest extends ApiServiceTestBase {

    @Autowired
    UrisBuilder urisBuilder;

    @Before
    public void setUp() {
        super.setUp();
        // Overrides connection information
        ReflectionTestUtils.setField(urisBuilder, "endSessionEndpoint", "http://localhost:9080/oidc/idp/logout");
        ReflectionTestUtils.setField(urisBuilder, "postLogoutRedirect", "http://localhost:3000/");
        ReflectionTestUtils.setField(urisBuilder, "clientId", "J3Wqyvlzgw0ySg");
        ReflectionTestUtils.setField(urisBuilder, "logoutUri", "/otherLogout");
        ReflectionTestUtils.setField(urisBuilder, "loginUri", "/otherLogin");
    }

    @Test
    public void shouldBuildLogoutRedirectUrl() throws Exception {

        // when
        String redirectionURLTest = "http://localhost:9080/oidc/idp/logout?client_id=J3Wqyvlzgw0ySg&post_logout_redirect_uri=http://localhost:3000/";

        // then
        assertThat(urisBuilder.buildLogoutRedirectUrl(), is(redirectionURLTest));

    }

    @Test
    public void shouldBuildLoginUriSettings() throws Exception {

        // then
        assertThat(urisBuilder.getLoginUriSettings().getName(), is("login"));
        assertThat(urisBuilder.getLoginUriSettings().getUri(), is("/otherLogin"));

    }

    @Test
    public void shouldBuildLogoutUriSettings() throws Exception {

        // then
        assertThat(urisBuilder.getLogoutUriSettings().getName(), is("logout"));
        assertThat(urisBuilder.getLogoutUriSettings().getUri(), is("/otherLogout"));

    }

}
