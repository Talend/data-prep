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

public class TQLFilterServiceTest extends AbstractFilterServiceTest {

    @Override
    protected FilterService getFilterService() {
        return new TQLFilterService();
    }

    @Test
    public void testValueMatch() throws Exception {
        // given
        final String tqlFilter = "0001 ~ '[a-z]+@dataprep.[a-z]+'";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        row.set("0001", "skermabon@dataprep.com");
        assertThatFilterExecutionReturnsTrueForRow("0001", "skermabon@dataprep.com");
        assertThatFilterExecutionReturnsFalseForRow("0001", "skermabon@talend.com");
    }

    @Test
    public void testValueIn() throws Exception {
        // given
        final String tqlFilter = "0001 in ['Vincent', 'François', 'Paul']'";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "Vincent");
        assertThatFilterExecutionReturnsFalseForRow("0001", "Stéphane");
    }

    @Override
    protected String givenFilter_0001_equals_toto() {
        return "0001 = 'toto'";
    }

    @Override
    protected String givenFilter_one_columns_equals_toto() {
        return "* = 'toto'";
    }

    @Override
    protected String givenFilter_0001_equals_5() {
        return "0001 = 5";
    }

    @Override
    protected String givenFilter_one_column_equals_5() {
        return "* = 5";
    }

    @Override
    protected String givenFilter_0001_equals_5dot35() {
        return "0001 = 5.35";
    }

    @Override
    protected String givenFilter_one_column_equals_5dot35() {
        return "* = 5.35";
    }

    @Override
    protected String givenFilter_0001_not_equal_test() {
        return "0001 != 'test'";
    }

    @Override
    protected String givenFilter_one_column_not_equal_test() {
        return "* != 'test'";
    }

    @Override
    protected String givenFilter_0001_not_equal_12() {
        return "0001 != 12";
    }

    @Override
    protected String givenFilter_one_column_not_equal_12() {
        return "* != 12";
    }

    @Override
    protected String givenFilter_0001_not_equal_24dot6() {
        return "0001 != 24.6";
    }

    @Override
    protected String givenFilter_one_column_not_equal_24dot6() {
        return "* != 24.6";
    }

    @Override
    protected String givenFilter_0001_greater_than_5() {
        return "0001 > 5";
    }

    @Override
    protected String givenFilter_one_column_greater_than_5() {
        return "* > 5";
    }

    @Override
    protected String givenFilter_0001_greater_than_minus0dot1() {
        return "0001 > -0.1";
    }

    @Override
    protected String givenFilter_one_column_greater_than_minus0dot1() {
        return "* > -0.1";
    }

    @Override
    protected String givenFilter_0001_less_than_5() {
        return "0001 < 5";
    }

    @Override
    protected String givenFilter_one_column_less_than_5() {
        return "* < 5";
    }

    @Override
    protected String givenFilter_0001_greater_or_equal_5() {
        return "0001 >= 5";
    }

    @Override
    protected String givenFilter_one_column_greater_or_equal_5() {
        return "* >= 5";
    }

    @Override
    protected String givenFilter_0001_less_or_equal_5() {
        return "0001 <= 5";
    }

    @Override
    protected String givenFilter_one_column_less_or_equal_5() {
        return "* <= 5";
    }

    @Override
    protected String givenFilter_0001_contains_toto() {
        return "0001 contains 'toto'";
    }

    @Override
    protected String givenFilter_0001_is_empty() {
        return "0001 is empty";
    }

    @Override
    protected String givenFilter_one_column_is_empty() {
        return "* is empty";
    }

    @Override
    protected String givenFilter_all_columns_complies_empty() {
        return "* complies ''";
    }

    @Override
    protected String givenFilter_0001_is_valid() {
        return "0001 is valid";
    }

    @Override
    protected String givenFilter_one_column_is_valid() {
        return "* is valid";
    }

    @Override
    protected String givenFilter_0001_is_invalid() {
        return "0001 is invalid";
    }

    @Override
    protected String givenFilter_one_column_is_invalid() {
        return "* is invalid";
    }

    @Override
    protected String givenFilter_0001_complies_Aa9dash() {
        return "0001 complies 'Aa9-'";
    }

    @Override
    protected String givenFilter_0001_complies_empty() {
        return "0001 complies ''";
    }

    @Override
    protected String givenFilter_0001_is_empty_AND_0002_equals_toto() {
        return "0001 is empty and 0002 = 'toto'";
    }

    @Override
    protected String givenFilter_0001_contains_data_OR_0002_equals_12dot3() {
        return "0001 contains 'data' or 0002 = 12.3";
    }

    @Override
    protected String givenFilter_0001_does_not_contain_word() {
        return "not (0001 contains 'word')";
    }

    @Override
    protected String givenFilter_0001_between_5_and_10() {
        return "0001 between [5, 10]";
    }

    @Override
    protected String givenFilter_one_column_between_5_and_10() {
        return "* between [5, 10]";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_and_timestampFor19900101() {
        final long secondsFrom_1970_01_01_UTC = (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000); // 1990-01-01
                                                                                                                      // UTC
                                                                                                                      // timezone
        return "0001 between [0, " + secondsFrom_1970_01_01_UTC + "]";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_and_timestampFor19900101() {
        final long secondsFrom_1970_01_01_UTC = (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000); // 1990-01-01
        // UTC
        // timezone
        return "* between [0, " + secondsFrom_1970_01_01_UTC + "]";
    }

}
