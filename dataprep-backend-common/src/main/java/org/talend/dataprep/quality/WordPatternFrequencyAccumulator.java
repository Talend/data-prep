package org.talend.dataprep.quality;

import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.recognition.RecognitionResult;

import java.util.Locale;
import java.util.Map;

public class WordPatternFrequencyAccumulator implements
        PatternAnalyzer.Accumulator<RecognitionResult, WordPatternFrequencyAccumulator.WordPatternFrequencyStatistics> {

    private WordPatternFrequencyStatistics statistics = new WordPatternFrequencyStatistics();

    @Override
    public void accept(RecognitionResult result) {
        for (Map.Entry<String, Locale> patternAndLocale : result.getPatternToLocale().entrySet()) {
            statistics.add(patternAndLocale);
        }
    }

    public WordPatternFrequencyStatistics get() {
        return statistics;
    }

    public static class WordPatternFrequencyStatistics extends PatternFrequencyStatistics {

    }
}
