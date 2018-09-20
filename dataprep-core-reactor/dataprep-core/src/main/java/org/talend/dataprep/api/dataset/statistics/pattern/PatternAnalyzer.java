package org.talend.dataprep.api.dataset.statistics.pattern;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.statistics.type.DataTypeEnum;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * Analyzer structure to handle rows to column analysis and caching. It needs:
 * * a function to build analysis result based on a record value and type
 * * an {@link Accumulator} to reduce each column analysis results to a single object.
 *
 * @param <T> the result type of the analyzer which is cached
 * @param <U> the accumulation result produced by the {@link Accumulator}.
 */
public class PatternAnalyzer<T, U> implements Analyzer<U> {

    /**
     * Analyzer.
     */
    private BiFunction<String, DataTypeEnum, T> analyzer;

    /**
     * Result holder.
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private AutoResizeList<Accumulator<T, U>> freqTableStatistics;

    /**
     * Column types.
     */
    private DataTypeEnum[] types;

    private Cache<Pair<String, DataTypeEnum>, T> valueRecognitionCache = Caffeine.newBuilder().maximumSize(400).build();

    public PatternAnalyzer(DataTypeEnum[] types, BiFunction<String, DataTypeEnum, T> analyzer,
            Supplier<Accumulator<T, U>> accumulatorSupplier) {
        this.analyzer = analyzer;
        this.types = types;
        freqTableStatistics = new AutoResizeList<>(accumulatorSupplier);
    }

    @Override
    public void init() {
    }

    @Override
    public boolean analyze(String... record) {
        freqTableStatistics.ensureSize(record.length);
        for (int valueIdx = 0; valueIdx < record.length; valueIdx++) {
            // column specific values
            Accumulator<T, U> accumulator = freqTableStatistics.get(valueIdx);
            String value = record[valueIdx];
            DataTypeEnum typeEnum = types == null ? DataTypeEnum.STRING : types[valueIdx];

            // get result with cache
            accumulator.accept(valueRecognitionCache.get(ImmutablePair.of(value, typeEnum), this::doAnalyse));
        }
        return true;
    }

    private T doAnalyse(Pair<String, DataTypeEnum> p) {
        return analyzer.apply(p.getLeft(), p.getRight());
    }

    @Override
    public List<U> getResult() {
        return freqTableStatistics.stream().map(Accumulator::get).collect(toList());
    }

    @Override
    public void end() {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to close
    }

    public interface Accumulator<T, U> extends Consumer<T> {

        U get();
    }

}
