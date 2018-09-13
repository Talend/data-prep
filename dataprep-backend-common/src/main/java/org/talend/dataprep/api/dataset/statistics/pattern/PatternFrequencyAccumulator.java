package org.talend.dataprep.api.dataset.statistics.pattern;

import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.recognition.RecognitionResult;

import java.util.Locale;
import java.util.Map;

public class PatternFrequencyAccumulator<T extends PatternFrequencyStatistics> implements
        PatternAnalyzer.Accumulator<RecognitionResult, T> {

    private T statistics;

    public PatternFrequencyAccumulator(T statistics) {
        this.statistics = statistics;
    }

    @Override
    public void accept(RecognitionResult result) {
        for (Map.Entry<String, Locale> patternAndLocale : result.getPatternToLocale().entrySet()) {
            statistics.add(patternAndLocale);
        }
    }

    public T get() {
        return statistics;
    }

}
