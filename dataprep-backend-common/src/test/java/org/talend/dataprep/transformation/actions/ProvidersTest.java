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

package org.talend.dataprep.transformation.actions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.transformation.actions.date.DateParser;

public class ProvidersTest {

    @Before
    public void setUp() throws Exception {
        Providers.clear();
        Providers.setProvider(Providers.DEFAULT_PROVIDER);
    }

    @Test
    public void shouldCreateDateParser() throws Exception {
        // when
        final DateParser dateParser1 = Providers.get();
        final DateParser dateParser2 = Providers.get(DateParser.class);

        // then
        assertTrue(dateParser1 == dateParser2);
    }

    @Test
    public void shouldCreateInstancesWithNoArg() throws Exception {
        // when
        final Object o = Providers.get(Object.class);

        // then
        assertNotNull(o);
    }

    @Test
    public void shouldCacheInstancesWithNoArg() throws Exception {
        // when
        final Object o1 = Providers.get(Object.class);
        final Object o2 = Providers.get(Object.class);

        // then
        assertNotNull(o1);
        assertNotNull(o2);
        assertTrue(o1 == o2);
    }

    @Test
    public void shouldCreateInstancesWithArg() throws Exception {
        // when
        final Object o = Providers.get(Integer.class, "1");

        // then
        assertNotNull(o);
        assertEquals(1, o);
    }

    @Test
    public void shouldCacheInstancesWithArg() throws Exception {
        // when
        final Object o1 = Providers.get(Integer.class, "1");
        final Object o2 = Providers.get(Integer.class, "1");

        // then
        assertNotNull(o1);
        assertNotNull(o2);
        assertTrue(o1 == o2);
    }

    @Test
    public void shouldCreateInstancesFromInterface() throws Exception {
        // when
        final Object o = Providers.get(ProviderTestInterface.class);

        // then
        assertNotNull(o);
        assertEquals(ProviderTestInterfaceImpl.class, o.getClass());
    }

    @Test
    public void shouldCreateInstancesFromInterfaceWithArg() throws Exception {
        // when
        final Object o = Providers.get(ProviderTestInterface.class, new Object());

        // then
        assertNotNull(o);
        assertEquals(ProviderTestInterfaceWithArgImpl.class, o.getClass());
    }

    @Test
    public void shouldCreateInstancesFromProvider() throws Exception {
        // when
        final Object INSTANCE = new Object();
        Providers.setProvider(new Providers.Provider() {

            @Override
            public <T> T get(Class<T> clazz, Object... args) {
                if (Object.class.equals(clazz)) {
                    return (T) INSTANCE;
                } else {
                    return null;
                }
            }

            @Override
            public void clear() {
                // Nothing to do
            }
        });
        final Object o1 = Providers.get(Object.class, new Object());
        final Object o2 = Providers.get(ProviderTestInterface.class);

        // then
        assertNotNull(o1);
        assertTrue(o1 == INSTANCE);
        assertNull(o2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWithInvalidArg() throws Exception {
        Providers.get(Object.class, "1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWithInvalidArgFromInterface() throws Exception {
        Providers.get(ProviderTestInterface.class, "1");
    }

    @Test
    public void shouldReturnDifferentInstancesWhenPrototypeScope() throws Exception {
        // Given
        final Object field = new Object();

        // When
        final InterfaceToImplementWithConstWithArgs firstInstanceWithArgs = Providers
                .get(InterfaceToImplementWithConstWithArgs.class, field);
        final InterfaceToImplementWithConstWithArgs secondInstanceWithArgs = Providers
                .get(InterfaceToImplementWithConstWithArgs.class, field);

        final InterfaceToImplementWithConstWithoutArgs firstInstanceWithoutArgs = Providers
                .get(InterfaceToImplementWithConstWithoutArgs.class);
        final InterfaceToImplementWithConstWithoutArgs secondInstanceWithoutArgs = Providers
                .get(InterfaceToImplementWithConstWithoutArgs.class);

        // Then
        assertTrue(secondInstanceWithArgs != firstInstanceWithArgs);
        assertEquals(ImplementationWithConstWithArgsPrototype.class, firstInstanceWithArgs.getClass());
        assertTrue(secondInstanceWithoutArgs != firstInstanceWithoutArgs);
        assertEquals(ImplementationWithConstWithoutArgsPrototype.class, firstInstanceWithoutArgs.getClass());
    }

    @Test
    public void shouldReturnSameInstanceWhenFakeScope() throws Exception {
        // Given
        final Object field = new Object();

        // When
        final Interface2ToImplementWithConstWithArgs firstInstanceWithArgs = Providers.get(Interface2ToImplementWithConstWithArgs.class, field);
        final Interface2ToImplementWithConstWithArgs secondInstanceWithArgs = Providers.get(Interface2ToImplementWithConstWithArgs.class, field);
        final Interface2ToImplementWithConstWithoutArgs firstInstanceWithoutArgs = Providers.get(Interface2ToImplementWithConstWithoutArgs.class);
        final Interface2ToImplementWithConstWithoutArgs secondInstanceWithoutArgs = Providers.get(Interface2ToImplementWithConstWithoutArgs.class);

        // Then
        assertTrue(secondInstanceWithArgs == firstInstanceWithArgs);
        assertEquals(ImplementationWithConstWithArgsFakeScope.class, firstInstanceWithArgs.getClass());
        assertTrue(secondInstanceWithoutArgs == firstInstanceWithoutArgs);
        assertEquals(ImplementationWithConstWithoutArgsFakeScope.class, firstInstanceWithoutArgs.getClass());
    }

    interface ProviderTestInterface {

        void test();
    }

    static class ProviderTestInterfaceImpl implements ProviderTestInterface {

        public ProviderTestInterfaceImpl() {
        }

        @Override
        public void test() {
            // Nothing to do
        }
    }

    public static class ProviderTestInterfaceWithArgImpl implements ProviderTestInterface {

        private final Object o;

        public ProviderTestInterfaceWithArgImpl(Object o) {
            this.o = o;
        }

        @Override
        public void test() {
            // Nothing to do
        }
    }

    @interface FakeScope {
    }

    interface InterfaceToImplementWithConstWithArgs {
    }

    interface Interface2ToImplementWithConstWithArgs {
    }

    interface InterfaceToImplementWithConstWithoutArgs {
    }

    interface Interface2ToImplementWithConstWithoutArgs {
    }

    @PrototypeScope
    public static class ImplementationWithConstWithArgsPrototype implements InterfaceToImplementWithConstWithArgs {

        private final Object o;

        public ImplementationWithConstWithArgsPrototype(Object o) {
            this.o = o;
        }
    }

    @FakeScope
    public static class ImplementationWithConstWithArgsFakeScope implements Interface2ToImplementWithConstWithArgs {

        public ImplementationWithConstWithArgsFakeScope(Object o) {
        }
    }

    @PrototypeScope
    public static class ImplementationWithConstWithoutArgsPrototype implements InterfaceToImplementWithConstWithoutArgs {

    }

    @FakeScope
    public static class ImplementationWithConstWithoutArgsFakeScope implements Interface2ToImplementWithConstWithoutArgs {

        public ImplementationWithConstWithoutArgsFakeScope() {
        }
    }

}
