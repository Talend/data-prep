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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.RowMetadata;

public class SimpleFilterServiceTest extends AbstractFilterServiceTest {

    @Test
    public void should_create_TRUE_predicate_on_empty_filter() throws Exception {
        // given
        final String filtersDefinition = "";

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThat(filter.test(row)).isTrue();
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_empty_object_definition() throws Exception {
        // given
        final String filtersDefinition = "{}";

        // when
        service.build(filtersDefinition, rowMetadata);

        // then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_throw_exception_on_invalid_definition() throws Exception {
        // given
        final String filtersDefinition = "}";

        // when
        service.build(filtersDefinition, rowMetadata);

        // then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_unknown_filter() throws Exception {
        // given
        final String filtersDefinition = "{" + //
                "   \"bouh\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";

        // when
        service.build(filtersDefinition, rowMetadata);

        // then
    }

    @Test
    public void should_create_CONTAINS_predicate_on_all() throws Exception {
        // given
        final String filtersDefinition = "{" + //
                "   \"contains\": {" + //
                "       \"value\": \"toto\"" + //
                "   }" + //
                "}";

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("toto", "toto") // both equals
                .assertFilterReturnsTrueForValues("Toto", "toto") // different case - equals
                .assertFilterReturnsTrueForValues("tatatoto", "toto") // contains but different - equals
                .assertFilterReturnsTrueForValues("tagada", "toto") // not contains - equals
                .assertFilterReturnsFalseForValues("tagada", "tagada"); // not contains - not contains
    }

    @Test
    public void should_create_number_RANGE_predicate_on_all() throws Exception {
        // given
        final String filtersDefinition = "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("4", "3")
                .assertFilterReturnsTrueForValues("6", "3"); // lt min
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid1() throws Exception {
        // given
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

        // when
        service.build(filtersDefinition, rowMetadata);

        // then
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_create_NOT_predicate_invalid2() throws Exception {
        // given
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
    protected String givenFilter_0001_between_5_incl_and_10_incl() {
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
    protected String givenFilter_0001_between_5_excl_and_10_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"lowerOpen\": true" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_5_incl_and_10_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_5_excl_and_10_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"lowerOpen\": true," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_5_incl_and_10_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_5_excl_and_10_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"lowerOpen\": true" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_5_incl_and_10_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_5_excl_and_10_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": \"5\"," + //
                "       \"end\": \"10\"," + //
                "       \"lowerOpen\": true," + //
                "       \"upperOpen\": false" + //
                "   }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"upperOpen\": false }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"lowerOpen\": true }" + //
                "}";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "}" + // 1990-01-01 UTC timezone
                "}";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"field\": \"0001\"," + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"lowerOpen\": true," + //
                "       \"upperOpen\": false }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"upperOpen\": false }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"lowerOpen\": true }" + //
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "}" + // 1990-01-01 UTC timezone
                "}";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl() {
        return "{" + //
                "   \"range\": {" + //
                "       \"start\": 0," + // 1970-01-01 UTC timezone
                "       \"end\": " + SECONDS_FROM_1970_01_01_UTC + "," + // 1990-01-01 UTC timezone
                "       \"lowerOpen\": true," + //
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
        assertThat(filter.test(row)).isTrue();
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
