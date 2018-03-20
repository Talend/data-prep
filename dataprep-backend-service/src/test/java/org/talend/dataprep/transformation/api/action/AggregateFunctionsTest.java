//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;

import org.junit.Test;

public class AggregateFunctionsTest {

    @Test
    public void testNullUniqueFunction() throws Exception {
        final BiFunction<Object, Object, Object> function = AggregateFunctions.aggregate(Collections.singletonList(null));
        assertThat(function.apply(null, null), nullValue());
    }

    @Test
    public void testNullFunctions() throws Exception {
        final BiFunction<Object, Object, Object> function = AggregateFunctions.aggregate(Arrays.asList(null, null));
        assertThat(function.apply(null, null), nullValue());
    }

    @Test
    public void testEmptyFunctions() throws Exception {
        final BiFunction<Object, Object, Object> function = AggregateFunctions.aggregate(Collections.emptyList());
        assertThat(function.apply(null, null), nullValue());
    }

    @Test
    public void testEmptyResults() throws Exception {
        final BiFunction<Object, Object, Object> function = AggregateFunctions.aggregate(Arrays.asList((o1, o2) -> null, (o1, o2) -> null));
        assertThat(function.apply(null, null), nullValue());
    }

    @Test
    public void testSingleFunction() throws Exception {
        final BiFunction<String, String, String> function = AggregateFunctions.aggregate(Collections.singletonList((s1, s2) -> s1 + s2));
        assertThat(function.apply(null, null), is("nullnull"));
        assertThat(function.apply("string1", "String2"), is("string1String2"));
    }

    @Test
    public void testMultipleFunction() throws Exception {
        final BiFunction<String, String, String> function = AggregateFunctions.aggregate(Arrays.asList((s1, s2) -> s1 + s2, (s1, s2) -> s2 + s1));
        assertThat(function.apply(null, null), is("nullnull"));
        assertThat(function.apply("string1", "String2"), is("String2string1"));
    }
}
