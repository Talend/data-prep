// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.common;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;

/**
 * Utility class for the ActionsMetadata
 */
@Component
public class ActionMetadataUtils implements ApplicationContextAware {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionMetadataUtils.class);

    private static final Map<String, Analyzer<Analyzers.Result>> analyzerCache = new HashMap<>();

    private static ApplicationContext applicationContext;

    /**
     * No argument constructor.
     */
    private ActionMetadataUtils() {
        // private constructor for utility class
    }

    /**
     * Check if the given value is invalid.
     *
     * First lookup the invalid values of the column metadata. Then call the ValueQualityAnalyzer in case the statistics
     * are not up to date or are not computed.
     *
     * @param colMetadata the column metadata.
     * @param value the value to check.
     * @return true if the value is invalid.
     */
    public static boolean checkInvalidValue(ColumnMetadata colMetadata, String value) {
        // easy case
        final Set<String> invalidValues = colMetadata.getQuality().getInvalidValues();
        if (invalidValues.contains(value)) {
            return true;
        }
        // Find analyzer for column type or domain type
        final AnalyzerService analyzerService = applicationContext.getBean(AnalyzerService.class);
        final Set<String> updatedInvalidValues;

        final String domain = colMetadata.getDomain();
        if (!StringUtils.isEmpty(domain)) {
            updatedInvalidValues = retrieveInvalidsFromCachedAnalyzer(analyzerService, domain, colMetadata, value);
        } else {
            final String type = colMetadata.getType();
            if (!StringUtils.isEmpty(type)) {
                updatedInvalidValues = retrieveInvalidsFromCachedAnalyzer(analyzerService, type, colMetadata, value);
            } else {
                // perform a data type only (no domain set).
                Analyzer<Analyzers.Result> analyzer = analyzerService.build(colMetadata, AnalyzerService.Analysis.QUALITY);
                try {
                    updatedInvalidValues = retrieveInvalids(analyzer, value);
                } finally {
                    try {
                        analyzer.close();
                    } catch (Exception e) {
                        LOGGER.warn("Unable to close analyzer.", e);
                    }
                }
            }
        }
        // update invalid values of column metadata to prevent unnecessary future analysis
        invalidValues.addAll(updatedInvalidValues);
        return updatedInvalidValues.contains(value);
    }

    /**
     * Retrieves invalids from cached analyzers
     * @param analyzerService the analyzer service
     * @param key the key used to map the analyzer
     * @param colMetadata the column metadata
     * @param value the value to check for validity
     * @return
     */
    private static Set<String> retrieveInvalidsFromCachedAnalyzer(AnalyzerService analyzerService, String key, ColumnMetadata colMetadata,
                                                                  String value) {
        final Set<String> result;

        Analyzer<Analyzers.Result> analyzer;
        synchronized (analyzerCache) {
            analyzer = analyzerCache.get(key);
            if (analyzer == null) {
                analyzer = analyzerService.build(colMetadata, AnalyzerService.Analysis.QUALITY);
                analyzerCache.put(key, analyzer);
            }
            // do not use analyzer.getResult().clear since it does not reinitialize the analyzers
            // TODO: change this method when DQ provide us with a new method to call for reinitialization
            result = retrieveInvalids(analyzer, value);
        }
        return result;
    }

    /**
     * Returns the set of invalid values
     * @param analyzer the analyzer used to check the value validity
     * @param value the value to check for validity
     * @return
     */
    private static Set<String> retrieveInvalids(Analyzer<Analyzers.Result> analyzer, String value) {
        final Set<String> result;
        analyzer.init();
        analyzer.analyze(value);
        analyzer.end();
        final List<Analyzers.Result> results = analyzer.getResult();
        if (results.isEmpty()) {
            LOGGER.warn("ValueQualityAnalysis of {} returned an empty result, invalid value could not be detected...");
            result = Collections.emptySet();
        } else {
            result = results.get(0).get(ValueQualityStatistics.class).getInvalidValues();
        }
        return result;
    }

    /**
     *
     * @return an unmodifiable version of the analyzer cache
     */
    public static Map<String, Analyzer<Analyzers.Result>> getAnalyzerCache() {
        return Collections.unmodifiableMap(analyzerCache);
    }

    /**
     * Re-initializes the analyzer cache
     */
    static void reinitializeCache(){
        analyzerCache.clear();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ActionMetadataUtils.applicationContext = applicationContext;
    }
}
