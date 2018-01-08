// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class OrderedBeans<T> implements Stream<T> {

    private final List<T> beans;

    public OrderedBeans(List<T> beans) {
        this.beans = new ArrayList<>(beans);
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return beans.stream().filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return beans.stream().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return beans.stream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return beans.stream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return beans.stream().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return beans.stream().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return beans.stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return beans.stream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return beans.stream().flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return beans.stream().distinct();
    }

    @Override
    public Stream<T> sorted() {
        return beans.stream().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return beans.stream().sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return beans.stream().peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return beans.stream().limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return beans.stream().skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        beans.stream().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        beans.stream().forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return beans.stream().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return beans.stream().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return beans.stream().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return beans.stream().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return beans.stream().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return beans.stream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return beans.stream().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return beans.stream().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return beans.stream().max(comparator);
    }

    @Override
    public long count() {
        return beans.stream().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return beans.stream().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return beans.stream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return beans.stream().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return beans.stream().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return beans.stream().findAny();
    }

    public static <T1> Builder<T1> builder() {
        return Stream.builder();
    }

    public static <T1> Stream<T1> empty() {
        return Stream.empty();
    }

    public static <T1> Stream<T1> of(T1 t1) {
        return Stream.of(t1);
    }

    public static <T1> Stream<T1> of(T1[] values) {
        return Stream.of(values);
    }

    public static <T1> Stream<T1> iterate(T1 seed, UnaryOperator<T1> f) {
        return Stream.iterate(seed, f);
    }

    public static <T1> Stream<T1> generate(Supplier<T1> s) {
        return Stream.generate(s);
    }

    public static <T1> Stream<T1> concat(Stream<? extends T1> a, Stream<? extends T1> b) {
        return Stream.concat(a, b);
    }

    @Override
    public Iterator<T> iterator() {
        return beans.stream().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return beans.stream().spliterator();
    }

    @Override
    public boolean isParallel() {
        return beans.stream().isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return beans.stream().sequential();
    }

    @Override
    public Stream<T> parallel() {
        return beans.stream().parallel();
    }

    @Override
    public Stream<T> unordered() {
        return beans.stream().unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return beans.stream().onClose(closeHandler);
    }

    @Override
    public void close() {
        beans.stream().close();
    }

    public void append(T item) {
        beans.add(item);
    }

}
