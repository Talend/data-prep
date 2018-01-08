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
package org.talend.dataprep.dataset.store.metadata.memory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;

import com.google.common.base.Defaults;

public class InMemoryDataSetMetadataRepositoryTest {

    private InMemoryDataSetMetadataRepository inMemoryDataSetMetadataRepository = new InMemoryDataSetMetadataRepository();

    private static final class TransientTestObject {

        transient boolean zeBoolean = true;

        transient char zeChar = 'R';

        transient byte zeByte = 12;

        transient short zeShort = 15;

        transient int zeInt = 18;

        transient long zeLong = 21;

        transient float zeFloat = 89;

        transient double zeDouble = 28;

        transient Object zeObject = 28;

        transient static boolean zeStaticBoolean = true;
    }

    @Test
    public void testResetTransiantOnDatasetMatadataFavorites() {
        TransientTestObject obj = new TransientTestObject();
        inMemoryDataSetMetadataRepository.resetTransientValues(obj);
        // check it has been reset to the default value
        assertTrue(Defaults.defaultValue(boolean.class).equals(obj.zeBoolean));
        assertTrue(Defaults.defaultValue(char.class).equals(obj.zeChar));
        assertTrue(Defaults.defaultValue(byte.class).equals(obj.zeByte));
        assertTrue(Defaults.defaultValue(short.class).equals(obj.zeShort));
        assertTrue(Defaults.defaultValue(int.class).equals(obj.zeInt));
        assertTrue(Defaults.defaultValue(float.class).equals(obj.zeFloat));
        assertTrue(Defaults.defaultValue(double.class).equals(obj.zeDouble));
        assertTrue(obj.zeObject == Defaults.defaultValue(Object.class));// cause it is null
        assertTrue(Defaults.defaultValue(boolean.class).equals(TransientTestObject.zeStaticBoolean));
    }

    @Test
    public void testCountAllDataSetsSizeReturnsZeroWhenRepoIsEmpty() {

        // when
        long totalSize = inMemoryDataSetMetadataRepository.countAllDataSetsSize();

        // then
        assertThat(totalSize, is(0L));
    }

    @Test
    public void testCountAllDataSetsSizeWhenRepoContainsMultipleElements() throws Exception {
        // given
        for (int i = 1; i <= 3; i++) {
            addMetadataToRepository(String.valueOf(i), 78945);
        }
        addMetadataToRepository("4", 5127892);

        // when
        long totalSize = inMemoryDataSetMetadataRepository.countAllDataSetsSize();

        // then
        assertThat(totalSize, is(5364727L));
    }

    private void addMetadataToRepository(String id, long dataSetSize) {
        DataSetMetadata metadata = new DataSetMetadata();
        metadata.setId(id);
        metadata.setDataSetSize(dataSetSize);
        inMemoryDataSetMetadataRepository.save(metadata);
    }

}
