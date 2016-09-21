package org.talend.dataprep.transformation.cache;

import org.junit.Test;
import org.talend.dataprep.cache.ContentCacheKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

public class TransformationMetadataCacheKeyTest {
    @Test
    public void getKey_should_generate_serialized_key() throws Exception {
        // given
        final ContentCacheKey key = new TransformationMetadataCacheKey("prep1", "step1", HEAD, "user1");

        // when
        final String keyStr = key.getKey();

        // then
        assertThat(keyStr, is("transformation-metadata_prep1_step1_HEAD_user1"));
    }

    @Test
    public void getMatcher_should_return_matcher_for_partial_key() throws Exception {
        // given
        final ContentCacheKey prepKey = new TransformationMetadataCacheKey("prep1", null, null, null);
        final ContentCacheKey stepKey = new TransformationMetadataCacheKey(null, "step1", null, null);
        final ContentCacheKey sourceKey = new TransformationMetadataCacheKey(null, null, HEAD, null);
        final ContentCacheKey userKey = new TransformationMetadataCacheKey(null, null, null, "user1");

        final ContentCacheKey matchingKey = new TransformationMetadataCacheKey("prep1", "step1", HEAD, "user1");
        final ContentCacheKey nonMatchingKey = new TransformationMetadataCacheKey("prep2", "step2", FILTER, "user2");

        // when / then
        assertThat(prepKey.getMatcher().test(matchingKey.getKey()), is(true));
        assertThat(stepKey.getMatcher().test(matchingKey.getKey()), is(true));
        assertThat(sourceKey.getMatcher().test(matchingKey.getKey()), is(true));
        assertThat(userKey.getMatcher().test(matchingKey.getKey()), is(true));

        assertThat(prepKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
        assertThat(stepKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
        assertThat(sourceKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
        assertThat(userKey.getMatcher().test(nonMatchingKey.getKey()), is(false));
    }

}