package org.talend.dataprep.quality;

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
    private List<Accumulator<T, U>> freqTableStatistics;

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

    private static class AutoResizeList<T> implements List<T> {

        private List<T> delegate;

        private Supplier<T> elementSupplier;

        AutoResizeList(Supplier<T> elementSupplier) {
            this.delegate = new ArrayList<>();
            this.elementSupplier = elementSupplier;
        }

        private void ensureSize(int targetIndex) {
            int currentSize = delegate.size();
            if (currentSize <= targetIndex) {
                for (int indexAdded = currentSize; indexAdded <= targetIndex; indexAdded++) {
                    delegate.add(elementSupplier.get());
                }
            }
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(T t) {
            return delegate.add(t);
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return delegate.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            return delegate.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public T get(int index) {
            ensureSize(index);
            return delegate.get(index);
        }

        @Override
        public T set(int index, T element) {
            ensureSize(index);
            return delegate.set(index, element);
        }

        @Override
        public void add(int index, T element) {
            ensureSize(index);
            delegate.add(index, element);
        }

        @Override
        public T remove(int index) {
            ensureSize(index);
            return delegate.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return delegate.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegate.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return delegate.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return delegate.listIterator(index);
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return delegate.subList(fromIndex, toIndex);
        }
    }
}
