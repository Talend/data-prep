package org.talend.dataprep.api.dataset.statistics.pattern;

import java.util.*;
import java.util.function.Supplier;

/**
 * List than is only filled with what the supplier build when {@link #ensureSize(int)} is called.
 */
class AutoResizeList<T> implements List<T> {

    private List<T> delegate;

    private Supplier<T> elementSupplier;

    AutoResizeList(Supplier<T> elementSupplier) {
        this.delegate = new ArrayList<>();
        this.elementSupplier = elementSupplier;
    }

    /**
     * fill the list up to supplied index using {@link Supplier<T>} to get values.
     *
     * @param targetIndex the new biggest index of the list queryable using {@link #get(int) get(targetIndex)}
     */
    public void ensureSize(int targetIndex) {
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
        return delegate.get(index);
    }

    @Override
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
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
