//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.TransformationBaseTest;

/**
 * Unit test for the TransformationCacheKey.
 * 
 * @see TransformationCacheKey
 */
public class TransformationCacheKeyTest extends TransformationBaseTest {

    /** When the unit test is created. */
    private Long now;

    /**
     * Default empty constructor.
     */
    public TransformationCacheKeyTest() {
        this.now = new Date().getTime();
    }

    @Test
    public void shouldGenerateKey() throws Exception {
        TransformationCacheKey key = getSampleKey("prep1", "good name", "JSON", "v1");
        assertNotNull(key.getKey());
    }

    @Test
    public void shouldGenerateSameKey() throws Exception {
        TransformationCacheKey key1 = getSampleKey("prep1", "good name", "JSON", "v3");
        TransformationCacheKey key2 = getSampleKey("prep1", "good name", "JSON", "v3");
        assertEquals(key1.getKey(), key2.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void headNotAllowed() throws Exception {
        getSampleKey("prep1", "good name", "JSON", "head");
    }

    private TransformationCacheKey getSampleKey(String prepId, String name, String format, String step) throws IOException {
        return new TransformationCacheKey(prepId, getDataSetMetadataSample(name), format, step);
    }

    private DataSetMetadata getDataSetMetadataSample(String name) {
        return metadataBuilder.metadata() //
                .id("123456789") //
                .created(now) //
                .name(name).mediaType("String").encoding("ISO-8859-1")
                .row(ColumnMetadata.Builder.column().name("col1").type(Type.STRING))
                .row(ColumnMetadata.Builder.column().name("col2").type(Type.STRING))
                .row(ColumnMetadata.Builder.column().name("col3").type(Type.STRING)).build();
    }
}