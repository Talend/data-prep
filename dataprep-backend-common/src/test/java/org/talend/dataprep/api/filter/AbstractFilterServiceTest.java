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
    protected static final long SECONDS_FROM_1970_01_01_UTC = (LocalDateTime.of(1990, JANUARY, 1, 0, 0).toEpochSecond(UTC) * 1000);

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

    protected void assertThatFilterExecutionReturnsTrueForRow(String columnId, String value) {
        row.set(columnId, value);
        assertThatFilterExecutionReturnsTrue();
    }

    protected void assertThatFilterExecutionReturnsTrue() {
        assertThat(filter.test(row)).isTrue();
    }

    protected void assertThatFilterExecutionReturnsFalseForRow(String columnId, String value) {
        row.set(columnId, value);
        assertThatFilterExecutionReturnsFalse();
    }

    protected void assertThatFilterExecutionReturnsFalse() {
        assertThat(filter.test(row)).isFalse();
    }

    protected void assertThatFilterExecutionReturnsTrueForRow(String[] columnIds, String[] values) {
        for (int i = 0; i < columnIds.length; i++) {
            row.set(columnIds[i], values[i]);
        }
        assertThatFilterExecutionReturnsTrue();
    }

    protected void assertThatFilterExecutionReturnsFalseForRow(String[] columnIds, String[] values) {
        for (int i = 0; i < columnIds.length; i++) {
            row.set(columnIds[i], values[i]);
        }
        assertThatFilterExecutionReturnsFalse();
    }

    private void assertFilterReturnsExpectedResultForRow(boolean expectedResult, String columnId, String value) {
        row.set(columnId, value);
        assertThat(filter.test(row)).isEqualTo(expectedResult);
    }

    private void assertFilterReturnsExpectedResultForRow(boolean expectedResult, String[] columnIds, String[] values) {
        for (int i = 0; i < columnIds.length; i++) {
            row.set(columnIds[i], values[i]);
        }
        assertThat(filter.test(row)).isEqualTo(expectedResult);
    }

    @Test
    public void testFilterShouldUnderstandAllDecimalSeparators() {
        // given
        final String filtersDefinition = givenFilter_0001_between_5_incl_and_10_incl();
        row.set("0001", "5.35");

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrue();

        row.set("0001", "5,35");
        assertThatFilterExecutionReturnsTrue();
    }

    @Test
    public void testEqualsPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "toto");
        assertThatFilterExecutionReturnsFalseForRow("0001", "Toto"); // different case
        assertThatFilterExecutionReturnsFalseForRow("0001", "tatatoto"); // contains but different
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // empty
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "toto");
    }

    protected abstract String givenFilter_0001_equals_toto();

    @Test
    public void testEqualsPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_columns_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "titi", "toto" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "toto", "titi" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "titi", "tata" });

        // invalid values are excluded
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "titi", "toto" });
        row.unsetInvalid("0002");
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "toto", "titi" });
    }

    protected abstract String givenFilter_one_columns_equals_toto();

    @Test
    public void testEqualsPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "5.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,00"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "05.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "0 005"); // eq

        assertThatFilterExecutionReturnsFalseForRow("0001", "3"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ".5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "5.0"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,00"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "05.0"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "0 005"); // eq but invalid
    }

    protected abstract String givenFilter_0001_equals_5();

    @Test
    public void testEqualsPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_equals_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5,00", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "05.0", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "0 005", "4.0" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "3.0", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4,5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { ",5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1 000.5", "4.0" });

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5,00", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "05.0", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "0 005", "4.0" });
    }

    protected abstract String givenFilter_one_column_equals_5();

    @Test
    public void testEqualsPredicateOnDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_equals_5dot35();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "5.35"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,35"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "05.35"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,3500"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,3500"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "0 005.35"); // eq

        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ".5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "5.35"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,35"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "05.35"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,3500"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,3500"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "0 005.35"); // eq but invalid
    }

    protected abstract String givenFilter_0001_equals_5dot35();

    @Test
    public void testEqualsPredicateOnDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_equals_5dot35();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5,35", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5,3500", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "0 005.35", "4.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5.35" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "05.35" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5,3500" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5,0", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { ",5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", ".5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "1 000.5" });

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5,35", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5,3500", "4.0" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "0 005.35", "4.0" });
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5.35" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "05.35" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5,3500" });
    }

    protected abstract String givenFilter_one_column_equals_5dot35();

    @Test
    public void testNotEqualPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_test();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        assertThatFilterExecutionReturnsTrueForRow("0001", "toto"); // neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "Test"); // neq

        assertThatFilterExecutionReturnsFalseForRow("0001", "test"); // eq

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsTrueForRow("0001", "test"); // eq but invalid so neq
    }

    protected abstract String givenFilter_0001_not_equal_test();

    @Test
    public void testNotEqualPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_test();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "toto", "titi" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "toto", "Test" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "toto", "test" });

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "toto", "test" });
    }

    protected abstract String givenFilter_one_column_not_equal_test();

    @Test
    public void testNotEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_12();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "12.1"); // neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "14"); // neq

        assertThatFilterExecutionReturnsFalseForRow("0001", "12"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "12.00"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "012,0"); // eq

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsTrueForRow("0001", "12"); // eq but invalid so neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "12.00"); // eq but invalid so neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "012,0"); // eq but invalid so neq
    }

    protected abstract String givenFilter_0001_not_equal_12();

    @Test
    public void testNotEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_12();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "12.1", "11.99" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "14", "11,99" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "12", "11.99" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "012,0", "11.99" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "12.1", "12.00" });

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "12", "11.99" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "012,0", "11.99" });
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "12.1", "12.00" });
    }

    protected abstract String givenFilter_one_column_not_equal_12();

    @Test
    public void testNotEqualPredicateOnDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_not_equal_24dot6();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "24"); // neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "26.6"); // neq

        assertThatFilterExecutionReturnsFalseForRow("0001", "24.60"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "24,6"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "024,60"); // eq

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsTrueForRow("0001", "24.60"); // eq but invalid so neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "24,6"); // eq but invalid so neq
        assertThatFilterExecutionReturnsTrueForRow("0001", "024,60"); // eq but invalid so neq
    }

    protected abstract String givenFilter_0001_not_equal_24dot6();

    @Test
    public void testNotEqualPredicateOnDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_not_equal_24dot6();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "24", "26.6" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "24.60", "11.99" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "024,60", "11.99" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "12.1", "24,6" });

        // invalid values matches the filter (as it is a 'not' one)
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "24.60", "11.99" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "024,60", "11.99" });
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "12.1", "24,6" });
    }

    protected abstract String givenFilter_one_column_not_equal_24dot6();

    @Test
    public void testGreaterThanPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "6"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "5"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "4"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ".5"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "5.0"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,00"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "05.0"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "0 005"); // eq

        assertThatFilterExecutionReturnsTrueForRow("0001", "5.5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "6"); // gt but invalid

        assertThatFilterExecutionReturnsFalseForRow("0001", "5.5"); // gt but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,5"); // gt but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt but invalid
    }

    protected abstract String givenFilter_0001_greater_than_5();

    @Test
    public void testGreaterThanPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "4", "6" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5", "4" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4", "2" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "toto", "" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "tata", null });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "4,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { ".5", ",5" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5,00" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "05.0", "0 005" });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "3.0", "5,5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "-1.000,5", "26.6" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.5", "1.6" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "24", "-1 000.5" });

        // invalid values are excluded
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4", "6" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "3.0", "5,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "-1.000,5", "26.6" });
        row.unsetInvalid("0002");
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.5", "1.6" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "24", "-1 000.5" });
    }

    protected abstract String givenFilter_one_column_greater_than_5();

    @Test
    public void testGreaterThanPredicateOnNegativeDecimalValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_than_minus0dot1();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "-0.05"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "1"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "-0.1"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "-2"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "-10.3"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "-0.05"); // gt but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "1"); // gt but invalid
    }

    protected abstract String givenFilter_0001_greater_than_minus0dot1();

    @Test
    public void testGreaterThanPredicateOnNegativeDecimalValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_than_minus0dot1();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "-1", "-0.05" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "-4", "6" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "-4", "-6.5" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "", "toto" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "tata", null });

        // invalid values are excluded
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "-1", "-0.05" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "-4", "6" });
    }

    protected abstract String givenFilter_one_column_greater_than_minus0dot1();

    @Test
    public void testGreaterThanOrEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_greater_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "6"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "5"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "4"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ".5"); // lt

        assertThatFilterExecutionReturnsTrueForRow("0001", "5.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,00"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "05.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "0 005"); // eq

        assertThatFilterExecutionReturnsTrueForRow("0001", "5.5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "05.0"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt but invalid
    }

    protected abstract String givenFilter_0001_greater_or_equal_5();

    @Test
    public void testGreaterThanOrEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_greater_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "6", "3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5", "-2" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4", "-2" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "Wolverine", "" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "X-Men", null });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "4,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { ".5", ",5" });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "2", "5,00" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "05.0", "3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "2", "0 005" });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5,5", "3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "4", "5.5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "1 000.5", "3" });

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "6", "3" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "3" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5,5", "3" });
    }

    protected abstract String givenFilter_one_column_greater_or_equal_5();

    @Test
    public void testLessThanPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_less_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalseForRow("0001", "6"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "5"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "4"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        assertThatFilterExecutionReturnsTrueForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", ".5"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "5.0"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,00"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "05.0"); // eq
        assertThatFilterExecutionReturnsFalseForRow("0001", "0 005"); // eq

        assertThatFilterExecutionReturnsFalseForRow("0001", "5.5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "4"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
    }

    protected abstract String givenFilter_0001_less_than_5();

    @Test
    public void testLessThanPredicateOnIntegerValueOoOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_less_than_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "6", "5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "6", "3" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "Iceberg", "" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "X-Men", null });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "5,5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "4,5", "8,5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "0.5", "12" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { ".5", "12" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", ",5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", ",5" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.0", "5,00" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "05.0", "0 005" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.5", "5,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "1 000.5" });

        // invalid values are excluded
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "6", "3" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "12", "0.5" });
    }

    protected abstract String givenFilter_one_column_less_than_5();

    @Test
    public void testLessThanOrEqualPredicateOnIntegerValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_less_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalseForRow("0001", "6"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "5"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "4"); // lt

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        assertThatFilterExecutionReturnsTrueForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsTrueForRow("0001", ".5"); // lt

        assertThatFilterExecutionReturnsTrueForRow("0001", "5.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,00"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "05.0"); // eq
        assertThatFilterExecutionReturnsTrueForRow("0001", "0 005"); // eq

        assertThatFilterExecutionReturnsFalseForRow("0001", "5.5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "5,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "5"); // eq but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "4"); // lt but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt but invalid
    }

    protected abstract String givenFilter_0001_less_or_equal_5();

    @Test
    public void testLessThanOrEqualPredicateOnIntegerValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_less_or_equal_5();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "7", "8" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", ",5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "3", ",7" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "Cyclops", "" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "X-Men", null });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "12.3" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "42", "4,5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", ",5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", ".5" });

        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", "5.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "5,00", "9.5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "7", "05.0" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "0 005", "9.5" });

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "5.5", "5,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "1 000.5" });

        // invalid values are excluded
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "7", ",5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "42", "4,5" });
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "3", ",7" });
    }

    protected abstract String givenFilter_one_column_less_or_equal_5();

    @Test
    public void testContainsPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_contains_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "toto"); // equals
        assertThatFilterExecutionReturnsTrueForRow("0001", "Toto"); // different case
        assertThatFilterExecutionReturnsTrueForRow("0001", "tatatoto"); // contains but different
        assertThatFilterExecutionReturnsFalseForRow("0001", "tagada"); // not contains

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // equals but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "Toto"); // different case but invalid
        assertThatFilterExecutionReturnsFalseForRow("0001", "tatatoto"); // contains but different but invalid
    }

    protected abstract String givenFilter_0001_contains_toto();

    @Test
    public void testContainsPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_contains_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] {"toto", "toto"}); // equals
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] {"toto", "titi"}); // equals
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] {"titi", "Toto"}); // different case
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] {"tatatoto", "titi"}); // contains but different
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] {"tagada", "titi"}); // not contains

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] {"toto", "titi"}); // equals but invalid
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] {"toto", "toto"}); // equals but invalid
    }

    protected abstract String givenFilter_one_column_contains_toto();

    @Test
    public void testCompliesPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_complies_Aa9dash();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // different pattern
        assertThatFilterExecutionReturnsTrueForRow("0001", "To5-"); // same pattern
        assertThatFilterExecutionReturnsFalseForRow("0001", "To5--"); // different length
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // empty value

        // invalid values are excluded
        row.setInvalid("0001");
        assertThatFilterExecutionReturnsFalseForRow("0001", "To5-"); // same pattern but invalid
    }

    protected abstract String givenFilter_0001_complies_Aa9dash();

    @Test
    public void testCompliesPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_complies_Aa9dash();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.set("0001", "toto"); // different pattern
        row.set("0002", "toto"); // different pattern
        assertThatFilterExecutionReturnsFalse();

        row.set("0001", "To5-"); // same pattern
        assertThatFilterExecutionReturnsTrue();

        row.set("0002", "To5-"); // different length
        assertThatFilterExecutionReturnsTrue();

        // TODO ELO to be continued...
        // invalid values are excluded
        row.setInvalid("0001");
    }

    protected abstract String givenFilter_one_column_complies_Aa9dash();

    @Test
    public void testCompliesEmptyPatternPredicateOnStringValue() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_complies_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", ""); // empty value
        assertThatFilterExecutionReturnsFalseForRow("0001", "tagada"); // not empty value
    }

    protected abstract String givenFilter_0001_complies_empty();

    @Test
    public void testCompliesEmptyPatternPredicateOnStringValueOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_complies_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "", "" }); // empty value
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "", "toto" }); // empty value
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "titi", "toto" }); // empty
                                                                                                                       // value
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "titi", "" }); // empty value
    }

    protected abstract String givenFilter_one_column_complies_empty();

    @Test
    public void testInvalidPredicateOnOneCell() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_invalid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.set("0001", "toto");
        row.setInvalid("0001"); // value in invalid array in column metadata
        assertThatFilterExecutionReturnsTrue();
        row.unsetInvalid("0001");
        assertThatFilterExecutionReturnsFalse();
    }

    protected abstract String givenFilter_0001_is_invalid();

    @Test
    public void testInvalidPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_invalid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.setInvalid("0001");
        row.unsetInvalid("0002");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "a", "b" });
        row.unsetInvalid("0001");
        row.setInvalid("0002");
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "", "" });
        row.unsetInvalid("0001");
        row.unsetInvalid("0002");
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "", "" });
    }

    protected abstract String givenFilter_one_column_is_invalid();

    @Test
    public void testValidPredicateOnOneCell() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_valid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.setInvalid("0001"); // value is marked as invalid
        assertThatFilterExecutionReturnsFalse();

        row.unsetInvalid("0001"); // value is marked as valid
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // empty

        row.unsetInvalid("0001"); // value is marked as valid
        assertThatFilterExecutionReturnsTrueForRow("0001", "toto"); // correct value
    }

    protected abstract String givenFilter_0001_is_valid();

    @Test
    public void testValidPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_valid();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.set("0001", "toto");
        row.set("0002", "toto");

        row.setInvalid("0001"); // value is marked as invalid
        assertThatFilterExecutionReturnsTrue();

        row.setInvalid("0002"); // value is marked as invalid
        assertThatFilterExecutionReturnsFalse();
    }

    protected abstract String givenFilter_one_column_is_valid();

    @Test
    public void testEmptyPredicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", ""); // empty
        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // not empty value
    }

    protected abstract String givenFilter_0001_is_empty();

    @Test
    public void testEmptyPredicateOnOneColumn() throws Exception {
        // given
        final String filtersDefinition = givenFilter_one_column_is_empty();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "Thor", "" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "Thor", "Odinson" });
    }

    protected abstract String givenFilter_one_column_is_empty();

    private void runTestBetweenPredicateOnNumberValue(String filtersDefinition, boolean isMinIncluded, boolean isMaxIncluded) throws Exception {
        // given
        // see method arguments

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.getRowMetadata().getById("0001").setType("integer");
        assertThatFilterExecutionReturnsFalseForRow("0001", "a"); // invalid number
        assertThatFilterExecutionReturnsFalseForRow("0001", "4"); // lt min
        assertThatFilterExecutionReturnsTrueForRow("0001", "8"); // in range
        assertThatFilterExecutionReturnsFalseForRow("0001", "20"); // gt max

        assertThatFilterExecutionReturnsFalseForRow("0001", "toto"); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", ""); // nan
        assertThatFilterExecutionReturnsFalseForRow("0001", null); // null

        assertThatFilterExecutionReturnsFalseForRow("0001", "4.5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", "4,5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ",5"); // lt
        assertThatFilterExecutionReturnsFalseForRow("0001", ".5"); // lt


        assertThatFilterExecutionReturnsTrueForRow("0001", "5.5"); // gt
        assertThatFilterExecutionReturnsTrueForRow("0001", "5,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1.000,5"); // gt
        assertThatFilterExecutionReturnsFalseForRow("0001", "1 000.5"); // gt

        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "5"); // min
        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "5.0"); // min
        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "5,00"); // min
        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "05.0"); // min
        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "0 005"); // min

        assertFilterReturnsExpectedResultForRow(isMaxIncluded, "0001", "10"); // max
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

    private void runTestBetweenPredicateOnNumberValueOnOneColumn(String filtersDefinition, boolean isMinIncluded, boolean isMaxIncluded)  throws Exception {
        // given
        // see method arguments

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.getRowMetadata().getById("0001").setType("integer");
        row.getRowMetadata().getById("0002").setType("integer");

        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "a", "4" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "a", "5.5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "a", "5,5" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "a", "8" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "toto", "20" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "", null });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "4.5", "4,5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { ",5", ".5" });
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "1.000,5", "1 000.5" });

        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "a", "5" });
        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "a", "5.0" });
        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "a", "5,00" });
        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "a", "05.0" });
        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "a", "0 005" });

        assertFilterReturnsExpectedResultForRow(isMaxIncluded, new String[] { "0001", "0002" }, new String[] { "a", "10" });
    }

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_closed() throws Exception {
        // [min, max]
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_incl_and_10_incl(), true, true);
    }

    protected abstract String givenFilter_one_column_between_5_incl_and_10_incl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_open() throws Exception {
        // ]min, max[
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_excl_and_10_excl(), false, false);
    }

    protected abstract String givenFilter_one_column_between_5_excl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_rightOpen() throws Exception {
        // [min, max[
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_incl_and_10_excl(), true, false);
    }

    protected abstract String givenFilter_one_column_between_5_incl_and_10_excl();

    @Test
    public void testBetweenPredicateOnNumberValueOnOneColumn_leftOpen() throws Exception {
        // ]min, max]
        runTestBetweenPredicateOnNumberValueOnOneColumn(givenFilter_one_column_between_5_excl_and_10_incl(), false, true);
    }

    protected abstract String givenFilter_one_column_between_5_excl_and_10_incl();

    private void runTestBetweenPredicateOnDateValue(String filtersDefinition, boolean isMinIncluded, boolean isMaxIncluded) throws Exception {
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
        assertThatFilterExecutionReturnsFalseForRow("0001", "a"); // invalid number
        assertThatFilterExecutionReturnsFalseForRow("0001", "1960-01-01"); // lt min
        assertThatFilterExecutionReturnsTrueForRow("0001", "1980-01-01"); // in range
        assertThatFilterExecutionReturnsFalseForRow("0001", "2000-01-01"); // gt max

        assertFilterReturnsExpectedResultForRow(isMinIncluded, "0001", "1970-01-01");
        assertFilterReturnsExpectedResultForRow(isMaxIncluded, "0001", "1990-01-01");
    }

    @Test
    public void testBetweenPredicateOnDateValue_closed() throws Exception {
        runTestBetweenPredicateOnDateValue(givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl(), true, true);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_incl();

    @Test
    public void testBetweenPredicateOnDateValue_open() throws Exception {
        runTestBetweenPredicateOnDateValue(givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl(), false, false);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValue_rigthOpen() throws Exception {
        runTestBetweenPredicateOnDateValue(givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl(), true, false);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_incl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValue_leftOpen() throws Exception {
        runTestBetweenPredicateOnDateValue(givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl(), false, true);
    }

    protected abstract String givenFilter_0001_between_timestampFor19700101_excl_and_timestampFor19900101_incl();

    private void runTestBetweenPredicateOnDateValueOnOneColumn(String filtersDefinition, boolean isMinIncluded, boolean isMaxExcluded) throws Exception {
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
        assertThatFilterExecutionReturnsFalseForRow(new String[] { "0001", "0002" }, new String[] { "a", "1960-01-01" });
        assertThatFilterExecutionReturnsTrueForRow(new String[] { "0001", "0002" }, new String[] { "a", "1980-01-01" });
        assertFilterReturnsExpectedResultForRow(isMinIncluded, new String[] { "0001", "0002" }, new String[] { "1960-01-01", "1970-01-01" });
        assertFilterReturnsExpectedResultForRow(isMaxExcluded, new String[] { "0001", "0002" }, new String[] { "1990-01-01", "2000-01-01" });
    }

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_closed() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl(), true, true);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_incl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_open() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl(), false, false);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_rightOpen() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl(), true, false);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_incl_and_timestampFor19900101_excl();

    @Test
    public void testBetweenPredicateOnDateValueOnOneColumn_leftOpen() throws Exception {
        runTestBetweenPredicateOnDateValueOnOneColumn(givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl(), false, true);
    }

    protected abstract String givenFilter_one_column_between_timestampFor19700101_excl_and_timestampFor19900101_incl();

    @Test
    public void should_create_AND_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_is_empty_AND_0002_equals_toto();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.set("0001", ""); // empty
        row.set("0002", "toto"); // eq value
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "tata"); // not empty
        row.set("0002", "toto"); // eq value
        assertThatFilterExecutionReturnsFalse();
        row.set("0001", ""); // empty
        row.set("0002", "tata"); // neq value
        assertThatFilterExecutionReturnsFalse();
    }

    protected abstract String givenFilter_0001_is_empty_AND_0002_equals_toto();

    @Test
    public void should_create_OR_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_contains_data_OR_0002_equals_12dot3();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        row.set("0001", "dataprep"); // contains
        row.set("0002", "12,30"); // eq value
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "toto"); // does not contain
        row.set("0002", "012.3"); // eq value
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "great data"); // contains
        row.set("0002", "12"); // neq value
        assertThatFilterExecutionReturnsTrue();
        row.set("0001", "tata"); // does not contain
        row.set("0002", "tata"); // neq value
        assertThatFilterExecutionReturnsFalse();
    }

    protected abstract String givenFilter_0001_contains_data_OR_0002_equals_12dot3();

    @Test
    public void should_create_NOT_predicate() throws Exception {
        // given
        final String filtersDefinition = givenFilter_0001_does_not_contain_word();

        // when
        filter = service.build(filtersDefinition, rowMetadata);

        // then
        assertThatFilterExecutionReturnsTrueForRow("0001", "another sentence"); // does not contain
        assertThatFilterExecutionReturnsFalseForRow("0001", "great wording"); // contains
    }

    protected abstract String givenFilter_0001_does_not_contain_word();

}
