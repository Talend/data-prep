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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;

public class TQLFilterServiceTest extends AbstractFilterServiceTest {

    @Override
    protected FilterService getFilterService() {
        return new TQLFilterService();
    }

    @Test
    public void testGetFilterColumnsMetadata() {
        // given
        final TQLFilterService tqlFilterService = new TQLFilterService();
        final RowMetadata rowMetadata = new RowMetadata();
        final ColumnMetadata firstColumn = new ColumnMetadata();
        firstColumn.setId("0000");
        firstColumn.setName("last name");
        final ColumnMetadata secondColumn = new ColumnMetadata();
        secondColumn.setId("0001");
        secondColumn.setName("First_Name COL");
        final ColumnMetadata thirdColumn = new ColumnMetadata();
        thirdColumn.setId("0002");
        thirdColumn.setName("The_e-mail");
        rowMetadata.setColumns(asList(firstColumn, secondColumn, thirdColumn));
        final String tqlFilter = "0002 ~ '[a-z]+@dataprep.[a-z]+' and 0001 in ['Vincent', 'François', 'Paul']";

        // when
        List<ColumnMetadata> filterColumns = tqlFilterService.getFilterColumnsMetadata(tqlFilter, rowMetadata);

        // then
        assertEquals(2, filterColumns.size());
        assertTrue(filterColumns.contains(secondColumn));
        assertTrue(filterColumns.contains(thirdColumn));
    }

    @Test
    public void testValueMatch() throws Exception {
        // given
        final String tqlFilter = "0001 ~ '[a-z]+@dataprep.[a-z]+'";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("skermabon@dataprep.com")
                .assertFilterReturnsFalseForValues("skermabon@talend.com");
    }

    @Test
    public void testOneColumnMatch() {
        // given
        final String tqlFilter = "* ~ '[a-z]+@dataprep.[a-z]+'";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("skermabon@dataprep", "skermabon@dataprep.com")
                .assertFilterReturnsFalseForValues("some_value", "whatever");
    }

    @Test
    public void testValueIn() throws Exception {
        // given
        final String tqlFilter = "0001 in ['Vincent', 'François', 'Paul']";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("Vincent")
                .assertFilterReturnsFalseForValues("Stéphane");
    }

    @Test
    public void testOneColumnIn() throws Exception {
        // given
        final String tqlFilter = "* in ['Vincent', 'François', 'Paul']";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("Vincent", "François")
                .assertFilterReturnsTrueForValues("Vincent", "Stéphane")
                .assertFilterReturnsFalseForValues("Nicolas", "Stéphane");
    }


    @Test
    public void testWhatever() throws Exception {
        // given
        final String tqlFilter = "0001 wordComplies '[word][digit]'";

        // when
        filter = service.build(tqlFilter, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("vincent1", "François")
                .assertFilterReturnsTrueForValues("Nicolas2", "Stéphane")
                .assertFilterReturnsFalseForValues("3Nicolas", "Stéphane");
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
    protected String givenFilter_one_column_contains_toto() {
        return "* contains 'toto'";
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
    protected String givenFilter_one_column_complies_Aa9dash() {
        return "* complies 'Aa9-'";
    }

    @Override
    protected String givenFilter_0001_complies_empty() {
        return "0001 complies ''";
    }

    @Override
    protected String givenFilter_one_column_complies_empty() {
        return "* complies ''";
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
    protected String givenFilter_0001_between_5_incl_and_10_incl() {
        return "0001 between [5, 10]";
    }

    @Override
    protected String givenFilter_0001_between_5_excl_and_10_excl() {
        return "0001 between ]5, 10[";
    }

    @Override
    protected String givenFilter_0001_between_5_incl_and_10_excl() {
        return "0001 between [5, 10[";
    }

    @Override
    protected String givenFilter_0001_between_5_excl_and_10_incl() {
        return "0001 between ]5, 10]";
    }

    @Override
    protected String givenFilter_one_column_between_5_incl_and_10_incl() {
        return "* between [5, 10]";
    }

    @Override
    protected String givenFilter_one_column_between_5_excl_and_10_excl() {
        return "* between ]5, 10[";
    }

    @Override
    protected String givenFilter_one_column_between_5_incl_and_10_excl() {
        return "* between [5, 10[";
    }

    @Override
    protected String givenFilter_one_column_between_5_excl_and_10_incl() {
        return "* between ]5, 10]";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl() {
        return "0001 between [0, " + SECONDS_FROM_1970_01_01_UTC + "]";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl() {
        return "0001 between ]0, " + SECONDS_FROM_1970_01_01_UTC + "[";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl() {
        return "0001 between [0, " + SECONDS_FROM_1970_01_01_UTC + "[";
    }

    @Override
    protected String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl() {
        return "0001 between ]0, " + SECONDS_FROM_1970_01_01_UTC + "]";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl() {
        return "* between [0, " + SECONDS_FROM_1970_01_01_UTC + "]";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl() {
        return "* between ]0, " + SECONDS_FROM_1970_01_01_UTC + "[";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl() {
        return "* between [0, " + SECONDS_FROM_1970_01_01_UTC + "[";
    }

    @Override
    protected String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl() {
        return "* between ]0, " + SECONDS_FROM_1970_01_01_UTC + "]";
    }

}
