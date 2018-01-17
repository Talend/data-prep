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

package org.talend.dataprep.conversions;

import static org.junit.Assert.*;
import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import org.junit.After;
import org.junit.Test;
import org.talend.dataprep.conversions.model.*;

public class BeanConversionServiceTest {

    private final BeanConversionService conversionService = new BeanConversionService();

    @After
    public void tearDown() throws Exception {
        conversionService.clear();
    }

    @Test
    public void shouldBeNullForNullObjectToConvert() throws Exception {
        assertNull(conversionService.convert(null, Object.class));
    }

    @Test
    public void shouldAllowConversionToSameClass() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class).build());

        // Then
        assertTrue(conversionService.has(ModelA.class));
        assertTrue(conversionService.canConvert(ModelA.class, ModelA.class));
    }

    @Test
    public void shouldAllowConversionToOtherRegisteredClasses() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class).toBeans(ModelA1.class, ModelA2.class).build());
        conversionService.register(fromBean(ModelB.class).build());

        // Then
        assertTrue(conversionService.has(ModelA.class));
        assertTrue(conversionService.canConvert(ModelA.class, ModelA1.class));
        assertTrue(conversionService.canConvert(ModelA.class, ModelA2.class));
        assertFalse(conversionService.canConvert(ModelA.class, ModelB.class));
    }

    @Test
    public void shouldConvertToOtherClass() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class).toBeans(ModelA1.class, ModelA2.class).build());

        // Then
        final ModelA1 convert = conversionService.convert(new ModelA("test"), ModelA1.class);
        assertEquals(ModelA1.class, convert.getClass());
        assertEquals("test", convert.getProperty());
    }

    @Test
    public void shouldConvertToSameClass() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class).toBeans(ModelA1.class, ModelA2.class).build());

        // Then
        final ModelA source = new ModelA("test");
        final ModelA convert = conversionService.convert(source, ModelA.class);
        assertTrue(source != convert);
        assertEquals(ModelA.class, convert.getClass());
        assertEquals("test", convert.getProperty());
    }

    @Test
    public void shouldConvertUsingCustom() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class) //
                .toBeans(ModelA1.class, ModelA2.class) //
                .using(ModelA2.class, (modelA, modelA2) -> { //
                    modelA2.setCustom("custom");
                    return modelA2;
                }) //
                .using(ModelA1.class, (modelA, modelA1) -> { //
                    modelA1.setProperty("Overridden at custom function");
                    return modelA1;
                }) //
                .build() //
        );

        // When
        final ModelA source = new ModelA("test");
        final ModelA1 modelA1 = conversionService.convert(source, ModelA1.class);
        final ModelA2 modelA2 = conversionService.convert(source, ModelA2.class);

        // Then
        assertEquals(ModelA1.class, modelA1.getClass());
        assertEquals("Overridden at custom function", modelA1.getProperty());

        assertEquals(ModelA2.class, modelA2.getClass());
        assertEquals("test", modelA2.getProperty());
        assertEquals("custom", modelA2.getCustom());
    }

    @Test
    public void shouldConvertUsingMultipleCustoms() throws Exception {
        // Given
        conversionService.register(fromBean(InheritedModel.class) //
                .toBeans(InheritedModelA.class, InheritedModelB.class) //
                .using(InheritedModelA.class, (modelA, inheritedModelA) -> { //
                    inheritedModelA.setInheritedModelA("inheritedA");
                    return inheritedModelA;
                }) //
                .using(InheritedModelB.class, (modelA, inheritedModelB) -> { //
                    inheritedModelB.setInheritedModelB("inheritedB");
                    return inheritedModelB;
                }) //
                .build() //
        );

        // When
        final InheritedModel source = new InheritedModel();
        final InheritedModelA inheritedModelA = conversionService.convert(source, InheritedModelA.class);
        final InheritedModelB inheritedModelB = conversionService.convert(source, InheritedModelB.class);

        // Then
        assertEquals("inheritedA", inheritedModelA.getInheritedModelA());
        assertEquals("inheritedB", inheritedModelB.getInheritedModelB());
        assertEquals("inheritedA", inheritedModelB.getInheritedModelA());
    }

    @Test
    public void shouldConvertToSubClass() throws Exception {
        // Given
        conversionService.register(fromBean(InheritedModel.class).toBeans(InheritedModelA.class).build());

        // When
        final InheritedModel source = new InheritedModel();
        source.setProperty("inherited");
        final InheritedModelA inheritedModelA = conversionService.convert(source, InheritedModelA.class);

        // Then
        assertEquals(InheritedModelA.class, inheritedModelA.getClass());
        assertEquals("inherited", inheritedModelA.getProperty());
    }

    @Test
    public void shouldConvertUsingOverride() throws Exception {
        // Given
        conversionService.register(fromBean(ModelA.class).toBeans(ModelA1.class, ModelA2.class).build());

        // When
        final ModelA1 convert = conversionService.convert(new ModelA("test"), ModelA1.class, (modelA, modelA1) -> {
            modelA1.setProperty("On the fly modified value");
            return modelA1;
        });

        // Then
        assertEquals(ModelA1.class, convert.getClass());
        assertEquals("On the fly modified value", convert.getProperty());
    }

    @Test
    public void shouldAmendExistingRegistration() throws Exception {
        // Given (similar to shouldConvertUsingCustom() but use 2 separate register calls).
        conversionService.register(fromBean(ModelA.class) //
                .toBeans(ModelA1.class) //
                .using(ModelA1.class, (modelA, modelA1) -> { //
                    modelA1.setProperty("Overridden at custom function");
                    return modelA1;
                }) //
                .build() //
        );
        conversionService.register(fromBean(ModelA.class) //
                .toBeans(ModelA2.class) //
                .using(ModelA2.class, (modelA, modelA2) -> { //
                    modelA2.setCustom("custom");
                    return modelA2;
                }) //
                .build() //
        );

        // When
        final ModelA source = new ModelA("test");
        final ModelA1 modelA1 = conversionService.convert(source, ModelA1.class);
        final ModelA2 modelA2 = conversionService.convert(source, ModelA2.class);

        // Then
        assertEquals(ModelA1.class, modelA1.getClass());
        assertEquals("Overridden at custom function", modelA1.getProperty());

        assertEquals(ModelA2.class, modelA2.getClass());
        assertEquals("test", modelA2.getProperty());
        assertEquals("custom", modelA2.getCustom());
    }

}
