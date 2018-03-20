// ============================================================================
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

package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talend.ServiceBaseTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class DataprepLocaleContextResolverIntegrationTest extends ServiceBaseTest {

    @Test
    public void shouldReturnOKWhenVersionAsked() throws Exception {
        Response response = RestAssured.get("/get_my_test_locale");

        assertEquals(200, response.getStatusCode());
        assertEquals(TEST_LOCALE, response.asString());
    }

    @Controller
    public static class TestController {

        @RequestMapping("get_my_test_locale")
        @ResponseBody
        public String getLocale() {
            return LocaleContextHolder.getLocale().toLanguageTag();
        }

    }
}
