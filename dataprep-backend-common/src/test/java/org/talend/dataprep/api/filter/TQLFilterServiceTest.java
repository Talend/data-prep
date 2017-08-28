// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class TQLFilterServiceTest extends FilterServiceTest {

    private TQLFilterService tqlFilterService = new TQLFilterService();

    @Test
    public void shouldMatchEquals() throws Exception {
        // Given
        row.set("0001", "test");

        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build("0001 = 'test'", rowMetadata);

        // Then
        assertTrue(predicate.test(row));
    }

    @Test
    public void shouldNotMatchEquals() throws Exception {
        // Given
        row.set("0001", "my value");

        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build("0001 = 'test'", rowMetadata);

        // Then
        assertFalse(predicate.test(row));
    }

    @Test
    public void shouldMatchGreaterThan() throws Exception {
        // Given
        row.set("0001", "0");

        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build("0001 > 1", rowMetadata);

        // Then
        assertFalse(predicate.test(row));
    }

    @Test
    public void shouldMatchLessThan() throws Exception {
        // Given
        row.set("0001", "0");

        // When
        final Predicate<DataSetRow> predicate = tqlFilterService.build("0001 < 1", rowMetadata);

        // Then
        assertTrue(predicate.test(row));
    }



}
