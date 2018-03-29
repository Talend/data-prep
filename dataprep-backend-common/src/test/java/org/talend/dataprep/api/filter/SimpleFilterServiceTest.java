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

package org.talend.dataprep.api.filter;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;

import java.time.LocalDateTime;

import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.RowMetadata;

public class SimpleFilterServiceTest extends AbstractFilterServiceTest {

    @Test
    public void should_create_TRUE_predicate_on_empty_filter() throws Exception {
        //given
        final String filtersDefinition = "";

        //when
        filter = service.build(filtersDefinition, rowMetadata);

        //then
        assertThatFilterExecutionReturnsTrue();
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_empty_object_definition() throws Exception {
        //given
        final String filtersDefinition = "{}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_invalid_definition() throws Exception {
        //given
        final String filtersDefinition = "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_unknown_filter() throws Exception {
        //given
        final String filtersDefinition = "{" + //
                "   \"bouh\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test
    public void should_create_CONTAINS_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" + //
                "   \"contains\": {" + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";

        //when
        filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "toto"); //equals
        row.set("0002", "toto"); //equals
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "Toto"); //different case
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "tatatoto"); //contains but different
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "tagada"); // not contains
        assertThatFilterExecutionReturnsTrue();
        row.set("0002", "tagada"); // not contains
        assertThatFilterExecutionReturnsFalse();
    }

    @Test
    public void should_create_number_RANGE_predicate_on_all() throws Exception {
        //given
        final String filtersDefinition = "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";

        //when
        filter = service.build(filtersDefinition, rowMetadata);

        //then
        row.set("0001", "4");
        row.set("0002", "3");
        assertThatFilterExecutionReturnsFalse();

        row.set("0001", "6"); //lt min
        assertThatFilterExecutionReturnsTrue();
    }

    @Test
    public void shouldNotIncludeInvalidValues() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_toto();
        row.set("0001", "toto");
        row.setInvalid("0001");

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalse();

        // given
        row.unsetInvalid("0001");

        // then
        assertThatFilterExecutionReturnsTrue();
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid1() throws Exception {
        //given
        final String filtersDefinition = "{" + //
                "   \"not\": [" + //
                "       {" + //
                "           \"empty\": {" + //
                "               \"field\": \"0001\"" + //
                "           }" + //
                "       }," + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0002\"," + //
                "               \"value\": \"toto\"" + //
                "           }" + //
                "       }" + //
                "   ]" + //
                "}";

        //when
        service.build(filtersDefinition, rowMetadata);

        //then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid2() throws Exception {
        //given
        final String filtersDefinition = "{" + //
                "   \"not\":" + //
                "       {" + //
                "       }" + //
                "}";

        // when
        service.build(filtersDefinition, rowMetadata);

        // then
    }

    @Override
    protected FilterService getFilterService() {
        return new SimpleFilterService();
    }

    @Override
    protected String givenFilter_0001_equals_toto() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_columns_equals_toto() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_equals_5() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"5\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_equals_5() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"value\": \"5\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_equals_5dot35() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"5.35\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_equals_5dot35() {
        return "{" + //
                "   \"eq\": {" + //
                "       \"value\": \"5.35\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_not_equal_test() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0001\"," + //
                "               \"value\": \"test\"" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_not_equal_test() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"value\": \"test\"" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_not_equal_12() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0001\"," + //
                "               \"value\": 12" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_not_equal_12() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"value\": 12" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_not_equal_24dot6() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0001\"," + //
                "               \"value\": 24.6" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_not_equal_24dot6() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"value\": 24.6" + //
                "           }" + //
                "       }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_greater_than_5() {
        return "{" + //
                "   \"gt\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_greater_than_5() {
        return "{" + //
                "   \"gt\": {" + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_greater_than_minus0dot1() {
        return "{" + //
                "   \"gt\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": -0.1" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_greater_than_minus0dot1() {
        return "{" + //
                "   \"gt\": {" + //
                "       \"value\": -0.1" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_greater_or_equal_5() {
        return "{" + //
                "   \"gte\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_greater_or_equal_5() {
        return "{" + //
                "   \"gte\": {" + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_less_than_5() {
        return "{" + //
                "   \"lt\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_less_than_5() {
        return "{" + //
                "   \"lt\": {" + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_less_or_equal_5() {
        return "{" + //
                "   \"lte\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_less_or_equal_5() {
        return "{" + //
                "   \"lte\": {" + //
                "       \"value\": 5" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_contains_toto() {
        return "{" + //
                "   \"contains\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_contains_toto() {
        return "{" + //
                "   \"contains\": {" + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_complies_Aa9dash() {
        return "{" + //
                "   \"matches\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"Aa9-\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_complies_Aa9dash() {
        return "{" + //
                "   \"matches\": {" + //
                "       \"value\": \"Aa9-\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_complies_empty() {
        return "{" + //
                "   \"matches\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_complies_empty() {
        return "{" + //
                "   \"empty\": {" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_is_invalid() {
        return "{" + //
                "   \"invalid\": {" + //
                "       \"field\": \"0001\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_is_invalid() {
        return "{" + //
                "   \"invalid\": {" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_is_valid() {
        return "{" + //
                "   \"valid\": {" + //
                "       \"field\": \"0001\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_is_valid() {
        return "{" + //
                "   \"valid\": {" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_is_empty() {
        return "{" + //
                "   \"empty\": {" + //
                "       \"field\": \"0001\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_is_empty() {
        return "{" + //
                "   \"empty\": {" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_5_and_10() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_5_and_10() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_and_timestampFor19900101() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                // 1990-01-01 UTC timezone
                "       \"end\": " + (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000) + "," + //
                "       \"upperOpen\": false }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_and_timestampFor19900101() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                // 1990-01-01 UTC timezone
                "       \"end\": " + (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000) + "," + //
                "       \"upperOpen\": false }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_is_empty_AND_0002_equals_toto() {
        return "{" + //
                "   \"and\": [" + //
                "       {" + //
                "           \"empty\": {" + //
                "               \"field\": \"0001\"" + //
                "           }" + //
                "       }," + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0002\"," + //
                "               \"value\": \"toto\"" + //
                "           }" + //
                "       }" + //
                "   ]" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_contains_data_OR_0002_equals_12dot3() {
        return "{" + //
                "   \"or\": [" + //
                "       {" + //
                "           \"contains\": {" + //
                "               \"field\": \"0001\"," + //
                "               \"value\": \"data\"" + //
                "           }" + //
                "       }," + //
                "       {" + //
                "           \"eq\": {" + //
                "               \"field\": \"0002\"," + //
                "               \"value\": \"12.3\"" + //
                "           }" + //
                "       }" + //
                "   ]" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_does_not_contain_word() {
        return "{" + //
                "   \"not\":" + //
                "       {" + //
                "           \"contains\": {" + //
                "               \"field\": \"0001\"," + //
                "               \"value\": \"word\"" + //
                "           }" + //
                "       }" + //
                "}";
    }

    /**
     * Make preparation sent to stream work when no filter is sent
     * <a>https://jira.talendforge.org/browse/TDP-3518</a>
     *
     */
    @Test
    public void TDP_3518_should_create_TRUE_predicate_on_double_quote() throws Exception {
        // given
        final String filtersDefinition = null;

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrue();
    }

    @Test
    public void TDP_4291_shouldNotThrowAnException() throws Exception {
        // given
        final String filtersDefinition = "{\"or\":[{\"invalid\":{}},{\"empty\":{}}]}";

        // when
        service.build(filtersDefinition, new RowMetadata());

        // then no NPE
    }

}
