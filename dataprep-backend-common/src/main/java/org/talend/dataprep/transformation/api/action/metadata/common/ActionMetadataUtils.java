package org.talend.dataprep.transformation.api.action.metadata.common;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataprep.configuration.AnalyzerService;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.ValueQualityStatistics;
import org.talend.datascience.common.inference.type.DataType;

/**
 * Utility class for the ActionsMetadata
 */
public class ActionMetadataUtils {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionMetadataUtils.class);

    private static final AnalyzerService service = new AnalyzerService();

    private static final Map<String, Analyzer<ValueQualityStatistics>> analyzerCache = new HashMap<>();

    /**
     * Default empty constructor.
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
        // Find analyzer for column type
        final String domain = colMetadata.getDomain();
        Analyzer<ValueQualityStatistics> analyzer;
        if (!StringUtils.isEmpty(domain)) {
            synchronized (analyzerCache) {
                analyzer = analyzerCache.get(domain);
                if (analyzer == null) {
                    analyzer = service.qualityAnalyzer(Collections.singletonList(colMetadata));
                    analyzer.init();
                    analyzerCache.put(domain, analyzer);
                }
                analyzer.getResult().clear();
                analyzer.analyze(value);
                analyzer.end();
            }
        } else {
            // perform a data type only (no domain set).
            DataType.Type[] types = TypeUtils.convert(Collections.singletonList(colMetadata));
            analyzer = new DataTypeQualityAnalyzer(types, true);
            analyzer.analyze(value);
            analyzer.end();
        }
        final List<ValueQualityStatistics> results = analyzer.getResult();
        if (results.isEmpty()) {
            LOGGER.warn("ValueQualityAnalysis of {} returned an empty result, invalid value could not be detected...");
            return false;
        }
        final Set<String> updatedInvalidValues = results.get(0).getInvalidValues();
        // update invalid values of column metadata to prevent unnecessary future analysis
        invalidValues.addAll(updatedInvalidValues);
        return updatedInvalidValues.contains(value);
    }

    public static Map<String, Analyzer<ValueQualityStatistics>> getAnalyzerCache() {
        return analyzerCache;
    }
}
