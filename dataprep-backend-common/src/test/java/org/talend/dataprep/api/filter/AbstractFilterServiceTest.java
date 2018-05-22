// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.date.DateParser;

public abstract class AbstractFilterServiceTest extends FilterServiceTest {

    /** 1990-01-01 UTC timezone */
    protected static final long SECONDS_FROM_1970_01_01_UTC =
            (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000);

    protected final FilterService service = getFilterService();

    protected Predicate<DataSetRow> filter;

    @Before
    public void setUp() {
        filter = null;
    }

    /**
     * Return a FilterService.
     *
     * @return an instance of FilterService
     */
    protected abstract FilterService getFilterService();

    protected FilterTest unlessInvalid(String... invalidColIds) {
        return new FilterTest(invalidColIds, true);
    }

    /**
     * Means that the filter execution should return the same result even if all columns are invalid for the given row.
     *
     * @return an object which helps to test the filter execution.
     */
    protected FilterTest whateverValidity() {
        return new FilterTest();
    }

    @Test
    public void testFilterShouldUnderstandAllDecimalSeparators() {
        // given
        final String filtersDefinition = givenFilter_0001_between_5_incl_and_10_incl();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("5.35")
                .assertFilterReturnsTrueForValues("5,35");
    }

    @Test
    public void testEqualsPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("toto");
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("Toto") // different case
                .assertFilterReturnsFalseForValues("tatatoto") // contains but different
                .assertFilterReturnsFalseForValues("") // empty
                .assertFilterReturnsFalseForValues(new String[] { null }); // null
    }

    protected abstract String givenFilter_0001_equals_toto();

    @Test
    public void testEqualsPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_columns_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("titi", "toto");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("toto", "titi");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("titi", "tata");
    }

    protected abstract String givenFilter_one_columns_equals_toto();

    @Test
    public void testEqualsPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("5.0") // eq
                .assertFilterReturnsTrueForValues("5,00") // eq
                .assertFilterReturnsTrueForValues("05.0") // eq
                .assertFilterReturnsTrueForValues("0 005"); // eq

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("3") // lt
                .assertFilterReturnsFalseForValues("4.5") // lt
                .assertFilterReturnsFalseForValues("4,5") // lt
                .assertFilterReturnsFalseForValues(",5") // lt
                .assertFilterReturnsFalseForValues(".5") // lt
                .assertFilterReturnsFalseForValues("1.000,5") // gt
                .assertFilterReturnsFalseForValues("1 000.5"); // gt
    }

    protected abstract String givenFilter_0001_equals_5();

    @Test
    public void testEqualsPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_equals_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("5.0", "4.0")
                .assertFilterReturnsTrueForValues("5.0", "4.0")
                .assertFilterReturnsTrueForValues("5,00", "4.0")
                .assertFilterReturnsTrueForValues("05.0", "4.0")
                .assertFilterReturnsTrueForValues("0 005", "4.0");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("3.0", "4.0")
                .assertFilterReturnsFalseForValues("4.5", "4.0")
                .assertFilterReturnsFalseForValues("4,5", "4.0")
                .assertFilterReturnsFalseForValues(",5", "4.0")
                .assertFilterReturnsFalseForValues("1.000,5", "4.0")
                .assertFilterReturnsFalseForValues("1 000.5", "4.0");
    }

    protected abstract String givenFilter_one_column_equals_5();

    @Test
    public void testEqualsPredicateOnDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_5dot35();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("5.35") // eq
                .assertFilterReturnsTrueForValues("5,35") // eq
                .assertFilterReturnsTrueForValues("05.35") // eq
                .assertFilterReturnsTrueForValues("5,3500") // eq
                .assertFilterReturnsTrueForValues("5,3500") // eq
                .assertFilterReturnsTrueForValues("0 005.35"); // eq

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("4.5") // lt
                .assertFilterReturnsFalseForValues("4,5") // lt
                .assertFilterReturnsFalseForValues(",5") // lt
                .assertFilterReturnsFalseForValues(".5") // lt
                .assertFilterReturnsFalseForValues("1.000,5") // gt
                .assertFilterReturnsFalseForValues("1 000.5"); // gt
    }

    protected abstract String givenFilter_0001_equals_5dot35();

    @Test
    public void testEqualsPredicateOnDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_equals_5dot35();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("5,35", "4.0")
                .assertFilterReturnsTrueForValues("5,3500", "4.0")
                .assertFilterReturnsTrueForValues("0 005.35", "4.0");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("5.0", "5.35")
                .assertFilterReturnsTrueForValues("5.0", "05.35")
                .assertFilterReturnsTrueForValues("5.0", "5,3500");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("5.0", "4.0")
                .assertFilterReturnsFalseForValues("5,0", "4.0")
                .assertFilterReturnsFalseForValues(",5", "4.0")
                .assertFilterReturnsFalseForValues("5.0", ".5")
                .assertFilterReturnsFalseForValues("1.000,5", "4.0")
                .assertFilterReturnsFalseForValues("5.0", "1 000.5");
    }

    protected abstract String givenFilter_one_column_equals_5dot35();

    @Test
    public void testNotEqualPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_test();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("toto") // neq
                .assertFilterReturnsTrueForValues("Test"); // neq

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("test"); // eq
    }

    protected abstract String givenFilter_0001_not_equal_test();

    @Test
    public void testNotEqualPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_test();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("toto", "titi")
                .assertFilterReturnsTrueForValues("toto", "Test");

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("toto", "test");
    }

    protected abstract String givenFilter_one_column_not_equal_test();

    @Test
    public void testNotEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_12();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("12.1") // neq
                .assertFilterReturnsTrueForValues("14"); // neq

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("12") // eq
                .assertFilterReturnsFalseForValues("12.00") // eq
                .assertFilterReturnsFalseForValues("012,0"); // eq
    }

    protected abstract String givenFilter_0001_not_equal_12();

    @Test
    public void testNotEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_12();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("12.1", "11.99")
                .assertFilterReturnsTrueForValues("14", "11,99");

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("12", "11.99")
                .assertFilterReturnsFalseForValues("012,0", "11.99");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("12.1", "12.00");
    }

    protected abstract String givenFilter_one_column_not_equal_12();

    @Test
    public void testNotEqualPredicateOnDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_24dot6();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("24") // neq
                .assertFilterReturnsTrueForValues("26.6"); // neq

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("24.60") // eq
                .assertFilterReturnsFalseForValues("24,6") // eq
                .assertFilterReturnsFalseForValues("024,60"); // eq
    }

    protected abstract String givenFilter_0001_not_equal_24dot6();

    @Test
    public void testNotEqualPredicateOnDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_24dot6();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("24", "26.6");

        // invalid values matches the filter (as it is a 'not' one)
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("24.60", "11.99")
                .assertFilterReturnsFalseForValues("024,60", "11.99");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("12.1", "24,6");
    }

    protected abstract String givenFilter_one_column_not_equal_24dot6();

    @Test
    public void testGreaterThanPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("6") // gt
                .assertFilterReturnsTrueForValues("5.5") // gt
                .assertFilterReturnsTrueForValues("5,5") // gt
                .assertFilterReturnsTrueForValues("1.000,5") // gt
                .assertFilterReturnsTrueForValues("1 000.5"); // gt

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("5") // eq
                .assertFilterReturnsFalseForValues("4") // lt

                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }) // null

                .assertFilterReturnsFalseForValues("4.5") // lt
                .assertFilterReturnsFalseForValues("4,5") // lt
                .assertFilterReturnsFalseForValues(",5") // lt
                .assertFilterReturnsFalseForValues(".5") // lt

                .assertFilterReturnsFalseForValues("5.0") // eq
                .assertFilterReturnsFalseForValues("5,00") // eq
                .assertFilterReturnsFalseForValues("05.0") // eq
                .assertFilterReturnsFalseForValues("0 005"); // eq
    }

    protected abstract String givenFilter_0001_greater_than_5();

    @Test
    public void testGreaterThanPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("4", "6")
                .assertFilterReturnsTrueForValues("3.0", "5,5")
                .assertFilterReturnsTrueForValues("-1.000,5", "26.6");
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("5.5", "1.6")
                .assertFilterReturnsTrueForValues("24", "-1 000.5");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("5", "4")
                .assertFilterReturnsFalseForValues("4", "2")

                .assertFilterReturnsFalseForValues("toto", "")
                .assertFilterReturnsFalseForValues("tata", null)

                .assertFilterReturnsFalseForValues("4.5", "4,5")
                .assertFilterReturnsFalseForValues(".5", ",5")

                .assertFilterReturnsFalseForValues("5.0", "5,00")
                .assertFilterReturnsFalseForValues("05.0", "0 005");
    }

    protected abstract String givenFilter_one_column_greater_than_5();

    @Test
    public void testGreaterThanPredicateOnNegativeDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_than_minus0dot1();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("-0.05") // gt
                .assertFilterReturnsTrueForValues("1"); // gt

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("-0.1") // eq
                .assertFilterReturnsFalseForValues("-2") // lt
                .assertFilterReturnsFalseForValues("-10.3") // lt

                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }); // null
    }

    protected abstract String givenFilter_0001_greater_than_minus0dot1();

    @Test
    public void testGreaterThanPredicateOnNegativeDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_than_minus0dot1();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("-1", "-0.05")
                .assertFilterReturnsTrueForValues("-4", "6");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("-4", "-6.5")
                .assertFilterReturnsFalseForValues("", "toto")
                .assertFilterReturnsFalseForValues("tata", null);
    }

    protected abstract String givenFilter_one_column_greater_than_minus0dot1();

    @Test
    public void testGreaterThanOrEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("6") // gt
                .assertFilterReturnsTrueForValues("5") // eq

                .assertFilterReturnsTrueForValues("5.0") // eq
                .assertFilterReturnsTrueForValues("5,00") // eq
                .assertFilterReturnsTrueForValues("05.0") // eq
                .assertFilterReturnsTrueForValues("0 005") // eq

                .assertFilterReturnsTrueForValues("5.5") // gt
                .assertFilterReturnsTrueForValues("5,5") // gt
                .assertFilterReturnsTrueForValues("1.000,5") // gt
                .assertFilterReturnsTrueForValues("1 000.5"); // gt

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("4") // lt

                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }) // null

                .assertFilterReturnsFalseForValues("4.5") // lt
                .assertFilterReturnsFalseForValues("4,5") // lt
                .assertFilterReturnsFalseForValues(",5") // lt
                .assertFilterReturnsFalseForValues(".5"); // lt
    }

    protected abstract String givenFilter_0001_greater_or_equal_5();

    @Test
    public void testGreaterThanOrEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("6", "3")
                .assertFilterReturnsTrueForValues("5", "-2")

                .assertFilterReturnsTrueForValues("5.0", "3")
                .assertFilterReturnsTrueForValues("05.0", "3")

                .assertFilterReturnsTrueForValues("5,5", "3")
                .assertFilterReturnsTrueForValues("1.000,5", "3")
                .assertFilterReturnsTrueForValues("1 000.5", "3");
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("2", "0 005")
                .assertFilterReturnsTrueForValues("4", "5.5")
                .assertFilterReturnsTrueForValues("2", "5,00");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("4", "-2")

                .assertFilterReturnsFalseForValues("Wolverine", "")
                .assertFilterReturnsFalseForValues("X-Men", null)

                .assertFilterReturnsFalseForValues("4.5", "4,5")
                .assertFilterReturnsFalseForValues(".5", ",5");
    }

    protected abstract String givenFilter_one_column_greater_or_equal_5();

    @Test
    public void testLessThanPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_less_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("4") // lt

                .assertFilterReturnsTrueForValues("4.5") // lt
                .assertFilterReturnsTrueForValues("4,5") // lt
                .assertFilterReturnsTrueForValues(",5") // lt
                .assertFilterReturnsTrueForValues(".5"); // lt

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("6") // gt
                .assertFilterReturnsFalseForValues("5") // eq
                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }) // null

                .assertFilterReturnsFalseForValues("5.0") // eq
                .assertFilterReturnsFalseForValues("5,00") // eq
                .assertFilterReturnsFalseForValues("05.0") // eq
                .assertFilterReturnsFalseForValues("0 005") // eq

                .assertFilterReturnsFalseForValues("5.5") // gt
                .assertFilterReturnsFalseForValues("5,5") // gt
                .assertFilterReturnsFalseForValues("1.000,5") // gt
                .assertFilterReturnsFalseForValues("1 000.5"); // gt
    }

    protected abstract String givenFilter_0001_less_than_5();

    @Test
    public void testLessThanPredicateOnIntegerValueOoOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_less_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("6", "3")
                .assertFilterReturnsTrueForValues("7", ",5")
                .assertFilterReturnsTrueForValues("7", ".5");
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("4.5", "5,5")
                .assertFilterReturnsTrueForValues("4,5", "8,5")
                .assertFilterReturnsTrueForValues("0.5", "12")
                .assertFilterReturnsTrueForValues(".5", "12");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("6", "5")

                .assertFilterReturnsFalseForValues("Iceberg", "")
                .assertFilterReturnsFalseForValues("X-Men", null)

                .assertFilterReturnsFalseForValues("5.0", "5,00")
                .assertFilterReturnsFalseForValues("05.0", "0 005")

                .assertFilterReturnsFalseForValues("5.5", "5,5")
                .assertFilterReturnsFalseForValues("1.000,5", "1 000.5");
    }

    protected abstract String givenFilter_one_column_less_than_5();

    @Test
    public void testLessThanOrEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_less_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("5") // eq
                .assertFilterReturnsTrueForValues("4") // lt

                .assertFilterReturnsTrueForValues("4.5") // lt
                .assertFilterReturnsTrueForValues("4,5") // lt
                .assertFilterReturnsTrueForValues(",5") // lt
                .assertFilterReturnsTrueForValues(".5") // lt

                .assertFilterReturnsTrueForValues("5.0") // eq
                .assertFilterReturnsTrueForValues("5,00") // eq
                .assertFilterReturnsTrueForValues("05.0") // eq
                .assertFilterReturnsTrueForValues("0 005"); // eq

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("6") // gt

                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }) // null

                .assertFilterReturnsFalseForValues("5.5") // gt
                .assertFilterReturnsFalseForValues("5,5") // gt
                .assertFilterReturnsFalseForValues("1.000,5") // gt
                .assertFilterReturnsFalseForValues("1 000.5"); // gt
    }

    protected abstract String givenFilter_0001_less_or_equal_5();

    @Test
    public void testLessThanOrEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_less_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("7", ",5")
                .assertFilterReturnsTrueForValues("42", "4,5")
                .assertFilterReturnsTrueForValues("7", ",5")
                .assertFilterReturnsTrueForValues("7", ".5")
                .assertFilterReturnsTrueForValues("7", "5.0")
                .assertFilterReturnsTrueForValues("7", "05.0");
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("4.5", "12.3")
                .assertFilterReturnsTrueForValues("5,00", "9.5")
                .assertFilterReturnsTrueForValues("0 005", "9.5");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("3", ",7");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("7", "8")

                .assertFilterReturnsFalseForValues("Cyclops", "")
                .assertFilterReturnsFalseForValues("X-Men", null)

                .assertFilterReturnsFalseForValues("5.5", "5,5")
                .assertFilterReturnsFalseForValues("1.000,5", "1 000.5");
    }

    protected abstract String givenFilter_one_column_less_or_equal_5();

    @Test
    public void testContainsPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_contains_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("toto") // equals
                .assertFilterReturnsTrueForValues("Toto") // different case
                .assertFilterReturnsTrueForValues("tatatoto"); // contains but different

        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("tagada"); // not contains
    }

    protected abstract String givenFilter_0001_contains_toto();

    @Test
    public void testContainsPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_contains_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("toto", "toto"); // equals
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("toto", "titi") // equals
                .assertFilterReturnsTrueForValues("tatatoto", "titi"); // contains but different
        // different case
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("titi", "Toto");

        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("tagada", "titi"); // not
                                                                                                            // contains
    }

    protected abstract String givenFilter_one_column_contains_toto();

    @Test
    public void testCompliesPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_complies_Aa9dash();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("To5-"); // same pattern
        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("toto") // different pattern
                .assertFilterReturnsFalseForValues("To5--") // different length
                .assertFilterReturnsFalseForValues(""); // empty value
    }

    protected abstract String givenFilter_0001_complies_Aa9dash();

    @Test
    public void testCompliesPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_complies_Aa9dash();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("To5-", "toto") // same pattern
                .assertFilterReturnsTrueForValues("To5-", "To5--"); // different length

        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("toto", "toto"); // different
                                                                                                          // pattern
    }

    protected abstract String givenFilter_one_column_complies_Aa9dash();

    @Test
    public void testCompliesEmptyPatternPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_complies_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues(""); // empty value

        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("tagada"); // not empty value
    }

    protected abstract String givenFilter_0001_complies_empty();

    @Test
    public void testCompliesEmptyPatternPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_complies_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        // empty values
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("", "");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("", "toto"); // empty value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("titi", ""); // empty value

        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("titi", "toto"); // not empty
                                                                                                          // values
    }

    protected abstract String givenFilter_one_column_complies_empty();

    @Test
    public void testInvalidPredicateOnOneCell() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_invalid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        unlessInvalid("0001").withColumns("0001").assertFilterReturnsFalseForValues("whatever");
    }

    protected abstract String givenFilter_0001_is_invalid();

    @Test
    public void testInvalidPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_invalid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        unlessInvalid("0001", "0002")
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("a", "b")
                .assertFilterReturnsFalseForValues("", "");
        unlessInvalid("0001")
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("a", "b")
                .assertFilterReturnsFalseForValues("", "");
        unlessInvalid("0002")
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("a", "b")
                .assertFilterReturnsFalseForValues("", "");
    }

    protected abstract String givenFilter_one_column_is_invalid();

    @Test
    public void testValidPredicateOnOneCell() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_valid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        unlessInvalid("0001").withColumns("0001").assertFilterReturnsTrueForValues("whatever"); // value
        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues(""); // empty
    }

    protected abstract String givenFilter_0001_is_valid();

    @Test
    public void testValidPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_valid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        unlessInvalid("0001", "0002").withColumns("0001", "0002").assertFilterReturnsTrueForValues("a", "b");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("", "");
    }

    protected abstract String givenFilter_one_column_is_valid();

    @Test
    public void testEmptyPredicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues(""); // empty
        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("whatever"); // value
    }

    protected abstract String givenFilter_0001_is_empty();

    @Test
    public void testEmptyPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("", "");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("", "whatever");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("a", "b");
    }

    protected abstract String givenFilter_one_column_is_empty();

    private void runTestBetweenPredicateOnNumberValue(String filtersDefinition, boolean isMinIncluded,
            boolean isMaxIncluded) throws Exception {
        // given
        // see method arguments

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.getRowMetadata().getById("0001").setType("integer");

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsTrueForValues("8") // in range

                .assertFilterReturnsTrueForValues("5.5") // gt
                .assertFilterReturnsTrueForValues("5,5"); // gt

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("a") // invalid number
                .assertFilterReturnsFalseForValues("4") // lt min
                .assertFilterReturnsFalseForValues("20") // gt max

                .assertFilterReturnsFalseForValues("toto") // nan
                .assertFilterReturnsFalseForValues("") // nan
                .assertFilterReturnsFalseForValues(new String[] { null }) // null

                .assertFilterReturnsFalseForValues("4.5") // lt
                .assertFilterReturnsFalseForValues("4,5") // lt
                .assertFilterReturnsFalseForValues(",5") // lt
                .assertFilterReturnsFalseForValues(".5") // lt

                .assertFilterReturnsFalseForValues("1.000,5") // gt
                .assertFilterReturnsFalseForValues("1 000.5"); // gt

        if (isMinIncluded) {
            whateverValidity()
                    .withColumns("0001")
                    .assertFilterReturnsTrueForValues("5") // min
                    .assertFilterReturnsTrueForValues("5.0") // min
                    .assertFilterReturnsTrueForValues("5,00") // min
                    .assertFilterReturnsTrueForValues("05.0") // min
                    .assertFilterReturnsTrueForValues("0 005"); // min
        } else {
            whateverValidity()
                    .withColumns("0001")
                    .assertFilterReturnsFalseForValues("5") // min
                    .assertFilterReturnsFalseForValues("5.0") // min
                    .assertFilterReturnsFalseForValues("5,00") // min
                    .assertFilterReturnsFalseForValues("05.0") // min
                    .assertFilterReturnsFalseForValues("0 005"); // min
        }
        if (isMaxIncluded) {
            whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("10"); // max
        } else {
            whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("10"); // max
        }
    }

    @Test
    public void testBetweenPredicateOnNumberValue_closed() throws Exception {
        runTestBetweenPredicateOnNumberValue(givenFilter_0001_between_5_incl_and_10_incl(), true, true);
    }

    protected abstract String givenFilter_0001_between_5_incl_and_10_incl();

    @Test
    public void testBetweenPredicateOnNumberValue_open() throws Exception {
        runTestBetweenPredicateOnNumberValue(givenFilter_0001_between_5_excl_and_10_excl(), false, false);
    }

    protected abstract String givenFilter_0001_between_5_excl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValue_rightOpen() throws Exception {
        runTestBetweenPredicateOnNumberValue(givenFilter_0001_between_5_incl_and_10_excl(), true, false);
    }

    protected abstract String givenFilter_0001_between_5_incl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValue_leftOpen() throws Exception {
        runTestBetweenPredicateOnNumberValue(givenFilter_0001_between_5_excl_and_10_incl(), false, true);
    }

    protected abstract String givenFilter_0001_between_5_excl_and_10_incl();

    private void runTestBetweenPredicateOnNumberValueOnOneColumn(String filtersDefinition, boolean isMinIncluded,
            boolean isMaxIncluded) throws Exception {
        // given
        // see method arguments

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.getRowMetadata().getById("0001").setType("integer");
        row.getRowMetadata().getById("0002").setType("integer");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsTrueForValues("a", "5.5")
                .assertFilterReturnsTrueForValues("a", "5,5")
                .assertFilterReturnsTrueForValues("a", "8");

        whateverValidity()
                .withColumns("0001", "0002")
                .assertFilterReturnsFalseForValues("a", "4")
                .assertFilterReturnsFalseForValues("toto", "20")
                .assertFilterReturnsFalseForValues("", null)
                .assertFilterReturnsFalseForValues("4.5", "4,5")
                .assertFilterReturnsFalseForValues(",5", ".5")
                .assertFilterReturnsFalseForValues("1.000,5", "1 000.5");

        if (isMinIncluded) {
            whateverValidity()
                    .withColumns("0001", "0002")
                    .assertFilterReturnsTrueForValues("a", "5") // min
                    .assertFilterReturnsTrueForValues("a", "5.0") // min
                    .assertFilterReturnsTrueForValues("a", "5,00") // min
                    .assertFilterReturnsTrueForValues("a", "05.0") // min
                    .assertFilterReturnsTrueForValues("a", "0 005"); // min
        } else {
            whateverValidity()
                    .withColumns("0001", "0002")
                    .assertFilterReturnsFalseForValues("a", "5") // min
                    .assertFilterReturnsFalseForValues("a", "5.0") // min
                    .assertFilterReturnsFalseForValues("a", "5,00") // min
                    .assertFilterReturnsFalseForValues("a", "05.0") // min
                    .assertFilterReturnsFalseForValues("a", "0 005"); // min
        }
        if (isMaxIncluded) {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("a", "10"); // max
        } else {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("a", "10"); // max
        }
    }

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_closed() throws Exception {
        // [min, max]
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_incl_and_10_incl(), true,
                true);
    }

    protected abstract String givenFilter_one_column_between_5_incl_and_10_incl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_open() throws Exception {
        // ]min, max[
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_excl_and_10_excl(), false,
                false);
    }

    protected abstract String givenFilter_one_column_between_5_excl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_rightOpen() throws Exception {
        // [min, max[
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_incl_and_10_excl(), true,
                false);
    }

    protected abstract String givenFilter_one_column_between_5_incl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_leftOpen() throws Exception {
        // ]min, max]
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_excl_and_10_incl(), false,
                true);
    }

    protected abstract String givenFilter_one_column_between_5_excl_and_10_incl();

    private void runTestBetweenPredicateOnDateValue(String filtersDefinition, boolean isMinIncluded,
            boolean isMaxIncluded) throws Exception {
        // given
        // see method arguments too
        final ColumnMetadata column = row.getRowMetadata().getById("0001");
        column.setType("date");
        final DateParser dateParser = Mockito.mock(DateParser.class);
        when(dateParser.parse("a", column)).thenThrow(new DateTimeException(""));
        when(dateParser.parse("1960-01-01", column)).thenReturn(LocalDateTime.of(1960, JANUARY, 1, 0, 0));
        when(dateParser.parse("1970-01-01", column)).thenReturn(LocalDateTime.of(1970, JANUARY, 1, 0, 0));
        when(dateParser.parse("1980-01-01", column)).thenReturn(LocalDateTime.of(1980, JANUARY, 1, 0, 0));
        when(dateParser.parse("1990-01-01", column)).thenReturn(LocalDateTime.of(1990, JANUARY, 1, 0, 0));
        when(dateParser.parse("2000-01-01", column)).thenReturn(LocalDateTime.of(2000, JANUARY, 1, 0, 0));

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("1980-01-01"); // in range

        whateverValidity()
                .withColumns("0001")
                .assertFilterReturnsFalseForValues("a") // invalid number
                .assertFilterReturnsFalseForValues("1960-01-01") // lt min
                .assertFilterReturnsFalseForValues("2000-01-01"); // gt max

        if (isMinIncluded) {
            whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("1970-01-01");
        } else {
            whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("1970-01-01");
        }
        if (isMaxIncluded) {
            whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("1990-01-01");
        } else {
            whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("1990-01-01");
        }
    }

    @Test
    public void testBetweenPredicateOnDateValue_closed() throws Exception {
        runTestBetweenPredicateOnDateValue(
                givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl(), true, true);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl();

    @Test
    public void testBetweenPredicateOnDateValue_open() throws Exception {
        runTestBetweenPredicateOnDateValue(
                givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl(), false, false);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValue_rigthOpen() throws Exception {
        runTestBetweenPredicateOnDateValue(
                givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl(), true, false);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValue_leftOpen() throws Exception {
        runTestBetweenPredicateOnDateValue(
                givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl(), false, true);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl();

    private void runTestBetweenPredicateOnDateValueOnOneColumn(String filtersDefinition, boolean isMinIncluded,
            boolean isMaxExcluded) throws Exception {
        // given
        // see method args too
        final DateParser dateParser = Mockito.mock(DateParser.class);

        final ColumnMetadata column1 = row.getRowMetadata().getById("0001");
        column1.setType("date");
        when(dateParser.parse("a", column1)).thenThrow(new DateTimeException(""));
        when(dateParser.parse("1960-01-01", column1)).thenReturn(LocalDateTime.of(1960, JANUARY, 1, 0, 0));
        when(dateParser.parse("1970-01-01", column1)).thenReturn(LocalDateTime.of(1970, JANUARY, 1, 0, 0));
        when(dateParser.parse("1980-01-01", column1)).thenReturn(LocalDateTime.of(1980, JANUARY, 1, 0, 0));
        when(dateParser.parse("1990-01-01", column1)).thenReturn(LocalDateTime.of(1990, JANUARY, 1, 0, 0));
        when(dateParser.parse("2000-01-01", column1)).thenReturn(LocalDateTime.of(2000, JANUARY, 1, 0, 0));

        final ColumnMetadata column2 = row.getRowMetadata().getById("0002");
        column2.setType("date");
        when(dateParser.parse("a", column2)).thenThrow(new DateTimeException(""));
        when(dateParser.parse("1960-01-01", column2)).thenReturn(LocalDateTime.of(1960, JANUARY, 1, 0, 0));
        when(dateParser.parse("1970-01-01", column2)).thenReturn(LocalDateTime.of(1970, JANUARY, 1, 0, 0));
        when(dateParser.parse("1980-01-01", column2)).thenReturn(LocalDateTime.of(1980, JANUARY, 1, 0, 0));
        when(dateParser.parse("1990-01-01", column2)).thenReturn(LocalDateTime.of(1990, JANUARY, 1, 0, 0));
        when(dateParser.parse("2000-01-01", column2)).thenReturn(LocalDateTime.of(2000, JANUARY, 1, 0, 0));

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("a", "1980-01-01");
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("a", "1960-01-01");
        if (isMinIncluded) {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("1960-01-01", "1970-01-01");
        } else {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("1960-01-01",
                    "1970-01-01");
        }
        if (isMaxExcluded) {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("1990-01-01", "2000-01-01");
        } else {
            whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("1990-01-01",
                    "2000-01-01");
        }
    }

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_closed() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(
                givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl(), true, true);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_open() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(
                givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl(), false, false);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_rightOpen() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(
                givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl(), true, false);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_leftOpen() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(
                givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl(), false, true);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl();

    @Test
    public void should_create_AND_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_empty_AND_0002_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        // empty -- eq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("", "toto");

        // not empty -- eq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("tata", "toto");

        // empty -- neq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("", "whatever");
    }

    protected abstract String givenFilter_0001_is_empty_AND_0002_equals_toto();

    @Test
    public void should_create_OR_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_contains_data_OR_0002_equals_12dot3();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        // contains -- eq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("dataprep", "12,30");

        // does not contain -- eq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("toto", "012.3");

        // contains -- neq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsTrueForValues("great data", "12");

        // does not contain -- neq value
        whateverValidity().withColumns("0001", "0002").assertFilterReturnsFalseForValues("tata", "5");
    }

    protected abstract String givenFilter_0001_contains_data_OR_0002_equals_12dot3();

    @Test
    public void should_create_NOT_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_does_not_contain_word();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        whateverValidity().withColumns("0001").assertFilterReturnsFalseForValues("great wording"); // contains
        whateverValidity().withColumns("0001").assertFilterReturnsTrueForValues("another sentence"); // does not contain
    }

    protected abstract String givenFilter_0001_does_not_contain_word();

    protected class FilterTest {

        private String[] invalidColIds;

        private String[] colIds;

        private boolean invalidityChangesResult;

        public FilterTest() {
            invalidColIds = new String[] {};
            invalidityChangesResult = false;
        }

        public FilterTest(String[] invalidColIds, boolean invalidityChangesResult) {
            this.invalidColIds = invalidColIds;
            this.invalidityChangesResult = invalidityChangesResult;
        }

        public FilterTest withColumns(String... coldIs) {
            this.colIds = coldIs;
            if (!invalidityChangesResult) {
                this.invalidColIds = this.colIds;
            }
            return this;
        }

        public FilterTest assertFilterReturnsTrueForValues(String... values) {
            assertFilterReturnsExpectedValue(true, values);
            return this;
        }

        private void assertFilterReturnsExpectedValue(boolean expected, String... values) {
            assertFilterReturnsExpectedResultForRow(expected, colIds, values);
            for (String invalidColId : invalidColIds) {
                row.setInvalid(invalidColId);
            }
            if (invalidityChangesResult) {
                assertFilterReturnsExpectedResultForRow(!expected, colIds, values);
            } else {
                assertFilterReturnsExpectedResultForRow(expected, colIds, values);
            }
            for (String invalidColId : invalidColIds) {
                row.unsetInvalid(invalidColId);
            }
        }

        private void assertFilterReturnsExpectedResultForRow(boolean expectedResult, String[] columnIds,
                String[] values) {
            for (int i = 0; i < columnIds.length; i++) {
                row.set(columnIds[i], values[i]);
            }
            assertThat(filter.test(row)).isEqualTo(expectedResult);
        }

        public FilterTest assertFilterReturnsFalseForValues(String... values) {
            assertFilterReturnsExpectedValue(false, values);
            return this;
        }

    }

}
