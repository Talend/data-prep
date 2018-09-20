package org.talend.dataprep.api.dataset.statistics.pattern;

import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;

/**
 * PatternFrequencyStatistics but as a different class to be available concurrently in the
 * {@link org.talend.dataquality.common.inference.Analyzers.Result} HashMap.
 */
public class WordPatternFrequencyStatistics extends PatternFrequencyStatistics {

}
