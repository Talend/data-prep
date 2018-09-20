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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilterTranslatorTest {

    private final FilterTranslator filterTranslator = new FilterTranslator();

    @Test
    public void testTranslateFiltersToTQL_LeaveFilterUnchanged_WhenAlreadyTQL() {
        // given
        String filter = "0001 = 'toto'";

        // when
        String tql = filterTranslator.toTQL(filter);

        // then
        assertEquals(filter, tql);
    }

    @Test
    public void testTranslateFiltersToTQL_TranslateColumnIdEqualsNumericValue() {
        // given
        final String filter = //
                "{" + //
                        "   \"eq\": {" + //
                        "       \"field\":\"0001\", " + //
                        "       \"value\": \"12\"" + //
                        "   }" + //
                        "}";
        final String expectedTQLFilter = "0001 = '12'";

        // when
        String tql = filterTranslator.toTQL(filter);

        // then
        assertEquals(expectedTQLFilter, tql);
    }

    @Test
    public void testTranslateFiltersToTQL_TranslateAndExpression() {
        // given
        final String filter = //
                "{" + //
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

        // when
        String tql = filterTranslator.toTQL(filter);

        // then
        String expectedTQLFilter = "(0001 is empty) and (0002 = 'toto')";
        assertEquals(expectedTQLFilter, tql);
    }

    @Test
    public void testToTQL_TranslateRangeExpression() {
        // given
        String filter = //
                "{" + //
                        "   \"range\": {" + //
                        "       \"field\": \"0001\"," + //
                        "       \"start\": \"5\"," + //
                        "       \"end\": \"10\"" + //
                        "   }" + //
                        "}";
        // when
        String tql = filterTranslator.toTQL(filter);

        // then
        String expectedTQLFilter = "0001 between [5, 10[";
        assertEquals(expectedTQLFilter, tql);
    }

    @Test
    public void testToTQL_TranslateCompliesExpression() {
        // given
        String filter = //
                "{" + //
                        "   \"matches\": {" + //
                        "       \"field\": \"0006\"," + //
                        "       \"value\": \"Aaaa\"" + //
                        "   }" + //
                        "}";

        // when
        String tql = filterTranslator.toTQL(filter);

        // then
        String expectedTQLFilter = "0006 complies 'Aaaa'";
        assertEquals(expectedTQLFilter, tql);
    }

}
