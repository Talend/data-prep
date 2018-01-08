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

package org.talend.dataprep.api.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TypeTest {

    @Test
    public void testList() {
        List<Type> anyList = Type.ANY.list();
        assertThat(anyList, hasItems(Type.ANY, Type.DOUBLE, Type.FLOAT, Type.INTEGER, Type.NUMERIC, Type.STRING, Type.BOOLEAN));
        List<Type> numericList = Type.NUMERIC.list();
        assertThat(numericList, hasItems(Type.DOUBLE, Type.FLOAT, Type.INTEGER, Type.NUMERIC));
    }

    @Test
    public void testTypeInheritance() throws Exception {
        Field[] fields = Type.class.getFields();
        for (Field field : fields) {
            Type type = (Type) field.get(null);
            // Assert the top level type is ANY for all types in Types class.
            Type lastType = type;
            Type topSuperType = type.getSuperType();
            while (topSuperType != null) {
                lastType = topSuperType;
                topSuperType = topSuperType.getSuperType();
            }
            assertThat(lastType, is(Type.ANY));
        }
    }

    @Test
    public void testName() {
        assertThat(Type.STRING.getName(), is("string"));
    }

    @Test
    public void testGetByNameArguments() {
        try {
            Type.get(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            Type type = Type.get("null");
            Assertions.assertThat(type).isEqualTo( Type.STRING );

        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetByName() throws Exception {
        Type type = Type.get("string");
        assertNotNull(type);
        assertEquals("string", type.getName());
    }

    @Test
    public void testIsAssignableFrom() throws Exception {
        assertTrue(Type.NUMERIC.isAssignableFrom(Type.INTEGER));
        assertTrue(Type.NUMERIC.isAssignableFrom(Type.DOUBLE));
        assertFalse(Type.NUMERIC.isAssignableFrom(Type.STRING));
    }

}
