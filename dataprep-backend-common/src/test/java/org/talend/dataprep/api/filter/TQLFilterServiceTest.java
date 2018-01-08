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

import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class TQLFilterServiceTest extends FilterServiceTest {

    private TQLFilterService tqlFilterService = new TQLFilterService();

    @Test
    public void testValueEquals() throws Exception {
        row.set("0001", "test");

        assertThatConditionIsTrue("0001 = 'test'");
        assertThatConditionIsFalse("0001 = 'my value'");
    }

    private void assertThatConditionIsTrue(String tqlCondition) {
        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build(tqlCondition, rowMetadata);

        // Then
        assertThat(predicate.test(row)).isTrue();
    }

    private void assertThatConditionIsFalse(String tqlCondition) {
        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build(tqlCondition, rowMetadata);

        // Then
        assertThat(predicate.test(row)).isFalse();
    }

    @Test
    public void testValueIsNotEqual() throws Exception {
        row.set("0001", "my value");

        assertThatConditionIsTrue("0001 != 'test'");
        assertThatConditionIsFalse("0001 != 'my value'");
    }

    @Test
    public void testValueIsGreaterThan() throws Exception {
        row.set("0001", "0");

        assertThatConditionIsTrue("0001 > -1");
        assertThatConditionIsFalse("0001 > 0");
    }

    @Test
    public void testValueIsLessThan() throws Exception {
        row.set("0001", "0");

        assertThatConditionIsTrue("0001 < 1");
        assertThatConditionIsFalse("0001 < 0");
    }

    @Test
    public void testValueIsGreaterOrEqualThan() throws Exception {
        row.set("0001", "1234");

        assertThatConditionIsTrue("0001 >= 1111");
        assertThatConditionIsTrue("0001 >= 1234");
        assertThatConditionIsFalse("0001 >= 2223");
    }

    @Test
    public void testValueIsLessOrEqualThan() throws Exception {
        row.set("0001", "10");

        assertThatConditionIsTrue("0001 <= 99");
        assertThatConditionIsTrue("0001 <= 10");
        assertThatConditionIsFalse("0001 <= 2");
    }

}
