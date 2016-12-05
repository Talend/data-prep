package org.talend.dataprep.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

@Configuration
public class Analyzers implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Analyzers.class);

    /** Where the data quality indexes are extracted (default to ${java.io.tmpdir}/org.talend.dataquality.semantic). */
    @Value("${dataquality.indexes.file.location:${java.io.tmpdir}/org.talend.dataquality.semantic}")
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
        return new AnalyzerService(dataqualityIndexesLocation, //
                luceneIndexStrategy, //
                CategoryRecognizerBuilder.newBuilder().lucene());
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Clean up analyzers...");
        ClassPathDirectory.destroy();
        LOGGER.info("Clean up analyzers done.");
    }

}
