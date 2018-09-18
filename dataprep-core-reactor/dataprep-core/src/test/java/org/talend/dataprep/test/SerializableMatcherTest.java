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

package org.talend.dataprep.test;

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SerializableMatcher.isNotSerializable;
import static org.talend.dataprep.test.SerializableMatcher.isSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class SerializableMatcherTest {

    private static class SerializableClass implements Serializable {
    }

    private static class NonSerializableFieldClass implements Serializable {

        private Object field = new Object();
    }

    private static class SerializableGenericFieldClass implements Serializable {

        private List<SerializableClass> field = new ArrayList<>();
    }

    private static class NonSerializableGenericFieldClass implements Serializable {

        private Set<Object> field = new HashSet<>();
    }

    private static class NonSerializableArrayFieldClass implements Serializable {

        private Object[] field = new Object[0];
    }

    private static class SerializableArrayFieldClass implements Serializable {

        private SerializableClass[] field = new SerializableClass[0];
    }

    @Test
    public void shouldReturnFalseForNonSerializable() throws Exception {
        assertThat(Object.class, isNotSerializable());
    }

    @Test
    public void shouldReturnTrueForSerializable() throws Exception {
        assertThat(SerializableClass.class, isSerializable());
    }

    @Test
    public void shouldReturnFalseForNonSerializableField() throws Exception {
        assertThat(NonSerializableFieldClass.class, isNotSerializable());
    }

    @Test
    public void shouldReturnTrueForSerializableGenericField() throws Exception {
        assertThat(SerializableGenericFieldClass.class, isSerializable());
    }

    @Test
    public void shouldReturnFalseForNonSerializableGenericField() throws Exception {
        assertThat(NonSerializableGenericFieldClass.class, isNotSerializable());
    }

    @Test
    public void shouldReturnTrueForSerializableArrayField() throws Exception {
        assertThat(SerializableArrayFieldClass.class, isSerializable());
    }

    @Test
    public void shouldReturnFalseForNonSerializableArrayField() throws Exception {
        assertThat(NonSerializableArrayFieldClass.class, isNotSerializable());
    }
}
