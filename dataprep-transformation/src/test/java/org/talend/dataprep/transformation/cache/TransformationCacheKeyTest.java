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

package org.talend.dataprep.transformation.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import org.junit.Test;
import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Unit test for the TransformationCacheKey.
 *
 * @see TransformationCacheKey
 */
public class TransformationCacheKeyTest {

    @Test
    public void shouldGenerateKey() throws Exception {
        // when
        final TransformationCacheKey key = createTestDefaultKey();

        // then
        assertNotNull(key.getKey());
    }

    @Test
    public void shouldGenerateSameKey() throws Exception {
        // given
        final TransformationCacheKey key1 = createTestDefaultKey();
        final TransformationCacheKey key2 = createTestDefaultKey(); // same params

        // then
        assertEquals(key1.getKey(), key2.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void headNotAllowed() throws Exception {
        // when
        new TransformationCacheKey( //
                "prep1", //
                "123456789", //
                "JSON", //
                "head", // this is not allowed
                "params", //
                FILTER, //
                "user 1", //
                "" // no filter
        );
    }

    @Test
    public void getKey_should_generate_serialized_key() throws Exception {
        // given
        final ContentCacheKey key = new TransformationCacheKey("prep1", "dataset1", "JSON", "step1", "param1", HEAD, "user1", "");

        // when
        final String keyStr = key.getKey();

        // then
        assertThat(keyStr, is("transformation_prep1_dataset1_b6aa01425c31e1eed71d0c3cbc7763aad865d1b1"));
    }

    @Test
    public void getMatcher_should_return_matcher_for_partial_key() throws Exception {
        // given
        final ContentCacheKey prepKey = new TransformationCacheKey("prep1", null, null, null, null, null, null, "");
        final ContentCacheKey dataSetKey = new TransformationCacheKey(null, "dataset1", null, null, null, null, null, "");
        final ContentCacheKey matchingKey = new TransformationCacheKey("prep1", "dataset1", "JSON", "step1", "param1", HEAD,
                "user1", "");
        final ContentCacheKey nonMatchingKey = new TransformationCacheKey("prep2", "dataset2", "XLS", "step2", "param2", FILTER,
                "user2", "");

        // when / then
        assertThat(prepKey.getMatcher().test(matchingKey.getKey()), is(true));
        assertThat(dataSetKey.getMatcher().test(matchingKey.getKey()), is(true));
        assertThat(prepKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
        assertThat(dataSetKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
    }

    private TransformationCacheKey createTestDefaultKey() {
        return new TransformationCacheKey( //
                "prep1", //
                "123456789", //
                "JSON", //
                "v1", //
                "params", //
                FILTER, //
                "user 1", //
                "" // no filter
        );
    }
}
