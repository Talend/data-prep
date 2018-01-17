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

import static junit.framework.TestCase.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class PolyglotFilterServiceTest extends FilterServiceTest {

    private final FilterService compositeFilterService = new PolyglotFilterService();

    @Test
    public void shouldUnderstandJsonQuery() throws Exception {
        //given
        row.set("c0001", "test");
        final String filtersDefinition = "{" +
                "   \"eq\": {" +
                "       \"field\": \"c0001\"," +
                "       \"value\": \"test\"" +
                "   }" +
                "}";

        //when
        final Predicate<DataSetRow> filter = compositeFilterService.build(filtersDefinition, rowMetadata);

        //then
       assertTrue(filter.test(row));
    }

    @Test
    public void shouldUnderstandTQLQuery() throws Exception {
        //given
        row.set("c0001", "test");
        final String filtersDefinition = "c0001 = 'test'";

        //when
        final Predicate<DataSetRow> predicate = compositeFilterService.build(filtersDefinition, rowMetadata);

        //then
        assertTrue(predicate.test(row));
    }
}
