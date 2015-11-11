package org.talend.dataprep.api.dataset;

import static java.util.stream.StreamSupport.stream;
import static org.talend.dataprep.api.dataset.diff.Flag.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.diff.FlagNames;
import org.talend.dataprep.api.type.Type;

/**
 * A DataSetRow is a row of a dataset. Values in data set row are <b>alphabetically</b> ordered by name.
 */
public class DataSetRow implements Cloneable {

    public static final String TDP_ID = "tdpId";

    /**
     * Filter for {@link #toArray(Predicate[])} that filters out TDP_ID column in results.
     */
    public static final Predicate<Map.Entry<String, String>> SKIP_TDP_ID = e -> !DataSetRow.TDP_ID.equals(e.getKey());

    /** Metadata information (columns...) about this DataSetRow */
    private final RowMetadata rowMetadata;

    /** Values of the dataset row. */
    private SortedMap<String, String> values = new TreeMap<>();

    /** True if this row is deleted. */
    private boolean deleted;

    /** the old value used for the diff. */
    private DataSetRow oldValue;

    /** Row id */
    private Long tdpId;

    /**
     * Constructor with values.
     */
    public DataSetRow(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
        this.deleted = false;
    }

    /**
     * Constructor with values.
     *
     * @param values the row value.
     */
    public DataSetRow(RowMetadata rowMetadata, Map<String, String> values) {
        this(rowMetadata);
        this.values.putAll(values);
    }

    public DataSetRow(Map<String, String> values) {
        this.values.putAll(values);
        List<ColumnMetadata> columns = values.keySet().stream() //
                .map(columnName -> ColumnMetadata.Builder.column().name(columnName).type(Type.STRING).build()) //
                .collect(Collectors.toList());
        rowMetadata = new RowMetadata(columns);
    }

    /**
     * @return The {@link RowMetadata metadata} that describes the current values.
     */
    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    /**
     * Set an entry in the dataset row
     *
     * @param name - the key
     * @param value - the value
     */
    public DataSetRow set(final String name, final String value) {
        values.put(name, value);
        return this;
    }

    /**
     * Get the value associated with the provided key
     *
     * @param id the column id.
     * @return - the value as string
     */
    public String get(final String id) {
        return values.get(id);
    }

    /**
     * Check if the row is deleted
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * Set whether the row is deleted
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Set the old row for diff
     *
     * @param oldRow - the original row
     */
    public void diff(final DataSetRow oldRow) {
        this.oldValue = oldRow;
    }

    /**
     * Here we decide the flags to set and write is to the response
     * <ul>
     * <li>flag NEW : deleted by old but not by new</li>
     * <li>flag UPDATED : not deleted at all and value has changed</li>
     * <li>flag DELETED : not deleted by old by is by new</li>
     * </ul>
     */
    public Map<String, Object> values() {

        final Map<String, Object> result = new LinkedHashMap<>(values.size() + 1);

        // if not old value, no diff to compute
        if (this.oldValue == null) {
            result.putAll(values);
            return result;
        }

        // row is no more deleted : we write row values with the *NEW* flag
        if (oldValue.isDeleted() && !isDeleted()) {
            result.put(FlagNames.ROW_DIFF_KEY, NEW.getValue());
            result.putAll(values);
        }
        // row has been deleted : we write row values with the *DELETED* flag
        else if (!oldValue.isDeleted() && isDeleted()) {
            result.put(FlagNames.ROW_DIFF_KEY, DELETE.getValue());
            result.putAll(oldValue.values());
        }

        // row has been updated : write the new values and get the diff for each value, then write the DIFF_KEY
        // property

        final Map<String, Object> diff = new HashMap<>();
        final Map<String, Object> originalValues = oldValue.values();

        // compute the new value (column is not found in old value)
        values.entrySet().stream().forEach(entry -> {
            if (!originalValues.containsKey(entry.getKey())) {
                diff.put(entry.getKey(), NEW.getValue());
            }
        });

        // compute the deleted values (column is deleted)
        originalValues.entrySet().stream().forEach(entry -> {
            if (!values.containsKey(entry.getKey())) {
                diff.put(entry.getKey(), DELETE.getValue());
                // put back the original entry so that the value can be displayed
                values.put(entry.getKey(), (String) entry.getValue());
            }
        });

        // compute the update values (column is still here but value is different)
        values.entrySet().stream().forEach(entry -> {
            if (originalValues.containsKey(entry.getKey())) {
                final Object originalValue = originalValues.get(entry.getKey());
                if (!StringUtils.equals(entry.getValue(), (String) originalValue)) {
                    diff.put(entry.getKey(), UPDATE.getValue());
                }
            }
        });

        result.putAll(values);
        if (!diff.isEmpty()) {
            result.put(FlagNames.DIFF_KEY, diff);
        }

        return result;
    }

    public Map<String, Object> valuesWithId() {
        final Map<String, Object> temp = values();
        if (getTdpId() != null) {
            temp.put(TDP_ID, getTdpId());
        }
        return temp;
    }

    /**
     * Clear all values in this row and reset state as it was when created (e.g. {@link #isDeleted()} returns
     * <code>false</code>).
     */
    public void clear() {
        deleted = false;
        oldValue = null;
        values.clear();
    }

    /**
     * @see Cloneable#clone()
     */
    @Override
    public DataSetRow clone() {
        final DataSetRow clone = new DataSetRow(rowMetadata, values);
        clone.setDeleted(this.isDeleted());
        clone.setTdpId(this.tdpId);
        return clone;
    }

    /**
     * Determine if the row should be written
     */
    public boolean shouldWrite() {
        if (this.oldValue == null) {
            return !isDeleted();
        } else {
            return !oldValue.isDeleted() || !isDeleted();
        }
    }

    /**
     * @see Objects#equals(Object, Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DataSetRow that = (DataSetRow) o;
        return Objects.equals(deleted, that.deleted) && Objects.equals(values, that.values);
    }

    /**
     * @see Objects#hash(Object...)
     */
    @Override
    public int hashCode() {
        return Objects.hash(deleted, values);
    }

    @Override
    public String toString() {
        return "DataSetRow{" + "deleted=" + deleted + ", oldValue=" + oldValue + ", values=" + values + '}';
    }

    /**
     * Order values of this data set row according to <code>columns</code>. This method clones the current record, so no
     * need to call {@link #clone()}.
     *
     * @param columns The columns to be used to order values.
     * @return A new data set row for method with values ordered following <code>columns</code>.
     */
    public DataSetRow order(List<ColumnMetadata> columns) {
        if (columns == null) {
            throw new IllegalArgumentException("Columns cannot be null.");
        }
        if (columns.isEmpty()) {
            return this;
        }
        if (columns.size() < values.size()) {
            throw new IllegalArgumentException("Expected " + values.size() + " columns but got " + columns.size());
        }

        List<String> idIndexes = columns.stream().map(ColumnMetadata::getId).collect(Collectors.toList());
        SortedMap<String, String> orderedValues = new TreeMap<>((id1, id2) -> idIndexes.indexOf(id1) - idIndexes.indexOf(id2));
        orderedValues.putAll(values);
        final DataSetRow dataSetRow = new DataSetRow(rowMetadata);
        dataSetRow.values = orderedValues;
        return dataSetRow;
    }

    /**
     * Returns the current row as an array of Strings.
     *
     * @param filters An optional set of {@link Predicate filters} to be used to filter values. See {@link #SKIP_TDP_ID}
     * for example.
     * @return The current row as array of String eventually with filtered out columns depending on filter.
     */
    @SafeVarargs
    public final String[] toArray(Predicate<Map.Entry<String, String>>... filters) {
        Stream<Map.Entry<String, String>> stream = stream(values.entrySet().spliterator(), false);
        // Apply filters
        for (Predicate<Map.Entry<String, String>> filter : filters) {
            stream = stream.filter(filter);
        }
        // Get as string array the selected columns
        final List<String> strings = stream.map(Map.Entry::getValue) //
                .map(String::valueOf) //
                .collect(Collectors.<String> toList());
        return strings.toArray(new String[strings.size()]);
    }

    public Long getTdpId() {
        return tdpId;
    }

    public void setTdpId(Long tdpId) {
        this.tdpId = tdpId;
    }

    /**
     * @return <code>true</code> if row has no value / or / only contains empty strings / or / null strings.
     * <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return values.isEmpty() || values.values().stream().filter(s -> !StringUtils.isEmpty(s)).count() == 0;
    }

    /**
     * @return A {@link DataSetRow} as 'unmodifiable': all previously set values cannot change (changes would be
     * silently ignored), setting a new column will set empty string (value will be discarded).
     */
    public DataSetRow unmodifiable() {
        return new UnmodifiableDataSetRow(this);
    }

    public DataSetRow modifiable() {
        return this;
    }

    /**
     * A wrapper implementation of {@link DataSetRow} that prevents changes on previous values and set empty string for
     * all new columns. This implementation allows modification on {@link RowMetadata}.
     * 
     * @see #set(String, String)
     */
    private static class UnmodifiableDataSetRow extends DataSetRow {

        private final DataSetRow delegate;

        private final boolean deleted;

        public UnmodifiableDataSetRow(DataSetRow delegate) {
            super(delegate.rowMetadata);
            this.delegate = delegate;
            deleted = delegate.isDeleted();
        }

        @Override
        public RowMetadata getRowMetadata() {
            return delegate.getRowMetadata();
        }

        /**
         * This method prevents changes on previous values and set empty string for all new columns.
         * 
         * @param name - the key A column name.
         * @param value - the value The value to be set for column name.
         * @return This data set row for chaining calls.
         */
        @Override
        public DataSetRow set(String name, String value) {
            if (delegate.get(name) == null) {
                return delegate.set(name, StringUtils.EMPTY);
            }
            return this;
        }

        @Override
        public String get(String id) {
            return delegate.get(id);
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }

        @Override
        public void setDeleted(boolean deleted) {
        }

        @Override
        public void diff(DataSetRow oldRow) {
            delegate.diff(oldRow);
        }

        @Override
        public Map<String, Object> values() {
            return Collections.unmodifiableMap(delegate.values());
        }

        @Override
        public Map<String, Object> valuesWithId() {
            return Collections.unmodifiableMap(delegate.valuesWithId());
        }

        @Override
        public void clear() {
        }

        @Override
        public DataSetRow clone() {
            return this;
        }

        @Override
        public boolean shouldWrite() {
            return delegate.shouldWrite();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public DataSetRow order(List<ColumnMetadata> columns) {
            return delegate.order(columns);
        }

        @Override
        public Long getTdpId() {
            return delegate.getTdpId();
        }

        @Override
        public void setTdpId(Long tdpId) {
        }

        @Override
        public DataSetRow unmodifiable() {
            return this;
        }

        @Override
        public DataSetRow modifiable() {
            return delegate;
        }
    }
}
