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

package org.talend.dataprep.api.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.datepattern.DatePattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

/**
 * Unit test for the datepattern api
 */
public class DatepatternAPITest extends ApiServiceTestBase {

    @Before
    public void cleanup() throws Exception {
        datePatternRepository.clear();
    }

    @Test
    public void list_empty_then_create_one_then_list_one() throws Exception {
        List<DatePattern> list = list("");
        Assertions.assertThat(list).isNotNull().isEmpty();

        DatePattern datePattern = new DatePattern("dd yyyy MMM");

        Response response = RestAssured.given() //
                .queryParam("pattern", datePattern.getPattern()) //
                .when() //
                .put("/api/datepatterns");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        list = list("");
        Assertions.assertThat(list).isNotNull().isNotEmpty().hasSize(1).contains(datePattern);
    }

    @Test
    public void list_empty_then_create_two_then_list_two() throws Exception {
        List<DatePattern> list = list("");
        Assertions.assertThat(list).isNotNull().isEmpty();

        DatePattern first = new DatePattern("dd yyyy MMM");

        Response response = RestAssured.given() //
                .queryParam("pattern", first.getPattern()) //
                .when() //
                .put("/api/datepatterns");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        DatePattern second = new DatePattern("DDD YYYY MMM");

        response = RestAssured.given() //
                .queryParam("pattern", second.getPattern()) //
                .when() //
                .put("/api/datepatterns");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        list = list("");
        Assertions.assertThat(list).isNotNull().isNotEmpty().hasSize(2).contains(first, second);

        list = list("DDD");
        Assertions.assertThat(list).isNotNull().isNotEmpty().hasSize(1).containsOnly(second);

    }

    private List<DatePattern> list(String keyword) throws Exception {
        Response response = RestAssured.given() //
                .when() //
                .queryParam("keyword", keyword) //
                .get("/api/datepatterns");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return mapper.readValue(response.asString(), new TypeReference<List<DatePattern>>() {
        });
    }
}
