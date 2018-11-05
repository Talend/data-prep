package org.talend.dataprep.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ZippedContentCache;
import org.talend.dataprep.processor.Wrapper;

@Configuration
public class ContentCacheConfiguration {

    @Bean
    @ConditionalOnProperty(name = "service.cache.zipped", havingValue = "true", matchIfMissing = true)
    public Wrapper<ContentCache> zippedContentCacheWrapper() {
        return new Wrapper<ContentCache>() {

            @Override
            public Class<ContentCache> wrapped() {
                return ContentCache.class;
            }

            @Override
            public ContentCache doWith(ContentCache instance, String beanName, ApplicationContext applicationContext) {
                return new ZippedContentCache(instance);
            }
        };
    }
}
