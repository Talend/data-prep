// ============================================================================
//
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

package org.talend.dataprep.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.springframework.util.ReflectionUtils;

public class UnitTestsUtil {

    public static <T> void injectFieldInClass(T object, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ReflectionUtils.findField(object.getClass(), fieldName);
        // Field field = object.getClass().getDeclaredField(fieldName);
        ReflectionUtils.makeAccessible(field);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        ReflectionUtils.setField(field, object, value);
    }

}
