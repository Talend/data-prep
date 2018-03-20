//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import com.google.common.base.MoreObjects;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.talend.dataprep.info.BuildDetails;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.ManifestInfoProvider;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@Import(VersionServiceAPITest.VersionTestConfiguration.class)
public class VersionServiceAPITest extends ApiServiceTestBase {

    @Test
    public void shouldReturnOKWhenVersionAsked() {
        BuildDetails buildDetails =
                expect().statusCode(200).when().get("/api/version").as(BuildDetails.class);

        Assert.assertEquals(4, buildDetails.getServices().length);
        Assert.assertEquals("GLOBAL_VERSION", buildDetails.getDisplayVersion());
    }

    @Test
    public void shouldReceiveSameVersionsWhenAskedTwice() {
        BuildDetails buildDetails = when().get("/api/version").as(BuildDetails.class);
        BuildDetails buildDetails2 = when().get("/api/version").as(BuildDetails.class);

        Assert.assertArrayEquals(buildDetails.getServices(), buildDetails2.getServices());
        Assert.assertEquals(buildDetails.getDisplayVersion(), buildDetails2.getDisplayVersion());
    }

    @Test
    public void shouldReceiveVersionsInCorrectOrder() {
        BuildDetails buildDetails = when().get("/api/version").as(BuildDetails.class);

        assertThat(buildDetails.getServices()).isNotEmpty();
        assertThat(buildDetails.getServices()).allMatch(v -> v.getVersionId().endsWith("-v2-id-v3-id"));
        assertThat(buildDetails.getServices()).allMatch(v -> v.getBuildId().endsWith("-v2-buildId-v3-buildId"));
    }

    @Configuration
    public static class VersionTestConfiguration {

        @Bean
        @Order(3)
        public ManifestInfoProvider version3ManifestProvider() {
            return new ManifestInfoProvider() {
                @Override
                public String getName() {
                    return "Version3";
                }

                @Override
                public ManifestInfo getManifestInfo() {
                    return new ManifestInfo("v3-id", "v3-buildId");
                }

                @Override
                public String toString() {
                    return MoreObjects.toStringHelper(this)
                            .add("name", getName())
                            .toString();
                }
            };
        }

        @Bean
        @Order(2)
        public ManifestInfoProvider version2ManifestProvider() {
            return new ManifestInfoProvider() {
                @Override
                public String getName() {
                    return "Version2";
                }

                @Override
                public ManifestInfo getManifestInfo() {
                    return new ManifestInfo("v2-id", "v2-buildId");
                }

                @Override
                public String toString() {
                    return MoreObjects.toStringHelper(this)
                            .add("name", getName())
                            .toString();
                }
            };
        }
    }
}
