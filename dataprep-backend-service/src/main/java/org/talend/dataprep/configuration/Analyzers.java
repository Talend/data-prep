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

package org.talend.dataprep.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.snapshot.StandardDictionarySnapshotProvider;

@Configuration
public class Analyzers implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Analyzers.class);

    /** Where the data quality indexes are extracted (default to ${java.io.tmpdir}/tdp/org.talend.dataquality.semantic). */
    @Value("${dataquality.indexes.file.location:${java.io.tmpdir}/tdp/org.talend.dataquality.semantic}")
    private String dataqualityIndexesLocation;

    @Value("#{'${luceneIndexStrategy:singleton}'}")
    private String luceneIndexStrategy;

    @Value("#{'${semantic.threshold:40}'}")
    private int semanticThreshold;

    @Bean
    public StatisticsAdapter statisticsAdapter() {
        return new StatisticsAdapter(semanticThreshold);
    }

    @Bean
    public AnalyzerService analyzerService() {
        LOGGER.info("Data Quality strategy is {} and located in {}", luceneIndexStrategy, dataqualityIndexesLocation);

        LOGGER.info("DataQuality indexes location : '{}'", this.dataqualityIndexesLocation);
        CategoryRegistryManager.setLocalRegistryPath(this.dataqualityIndexesLocation);

        // Configure DQ index creation strategy (one copy per use or one copy shared by all calls).
        LOGGER.info("Analyzer service lucene index strategy set to '{}'", luceneIndexStrategy);
        if ("basic".equalsIgnoreCase(luceneIndexStrategy)) {
            ClassPathDirectory.setProvider(new ClassPathDirectory.BasicProvider());
        } else if ("singleton".equalsIgnoreCase(luceneIndexStrategy)) {
            ClassPathDirectory.setProvider(new ClassPathDirectory.SingletonProvider());
        } else {
            // Default
            LOGGER.warn("Not a supported strategy for lucene indexes: '{}'", luceneIndexStrategy);
            ClassPathDirectory.setProvider(new ClassPathDirectory.SingletonProvider());
        }

        LOGGER.info("DataQuality indexes location : '{}'", this.dataqualityIndexesLocation);
        return new AnalyzerService(new StandardDictionarySnapshotProvider());
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Clean up analyzers...");
        ClassPathDirectory.destroy();
        LOGGER.info("Clean up analyzers done.");
    }

}
