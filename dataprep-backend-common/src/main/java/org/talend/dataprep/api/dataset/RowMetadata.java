package org.talend.dataprep.api.dataset;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.Column;
import org.talend.dataprep.api.dataset.diff.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models metadata information for a row of a data set.
 */
public class RowMetadata implements Serializable {

    /** List of row metadata. */
    @JsonProperty("ColumnMetadata")
    private List<ColumnMetadata> columns = new ArrayList<>();

    /**
     * Default empty constructor.
     */
    public RowMetadata() {
        // nothing special here
    }

    /**
     * Default constructor.
     * 
     * @param columns the list of column metadata.
     */
    public RowMetadata(List<ColumnMetadata> columns) {
        setColumns(columns);
    }

    /**
     * @return The metadata of this row's columns.
     */
    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    /**
     * @param columnMetadata the metadata to set.
     */
    public void setColumns(List<ColumnMetadata> columnMetadata) {
        columns.clear();
        columnMetadata.forEach(this::addColumn);
    }

    private ColumnMetadata addColumn(ColumnMetadata columnMetadata) {
        return addColumn(columnMetadata, columns.size());
    }

    /**
     * Removes the column metadata corresponding to the specified id from the columns of this row metadata, and returns
     * <tt>the deleted column metadata</tt> if it is present. If the specified id does not correspond to a column
     * metadata or if this row metadata does not contain the specified column metadata, the row metadata remains
     * unchanged and returns <tt>null</tt>.
     *
     * @param id the specified id corresponding to a column metadata to be deleted
     * @return <tt>the deleted column metadata</tt> if the column metadata corresponding to the specified id is in this
     * row metadata and <tt>null</tt> otherwise
     */
    public ColumnMetadata deleteColumnById(String id) {
        ColumnMetadata column = getById(id);
        if (column != null && columns.remove(column)) {
            return column;
        }
        return null;
    }

    private ColumnMetadata addColumn(ColumnMetadata columnMetadata, int index) {
        DecimalFormat format = new DecimalFormat("0000"); //$NON-NLS-1$
        if (StringUtils.isEmpty(columnMetadata.getId())) {
            columnMetadata.setId(format.format(this.columns.size()));
        }
        columns.add(index, columnMetadata);
        return columnMetadata;
    }

    /**
     * @return the row size.
     */
    public int size() {
        return columns.size();
    }

    /**
     * @param wantedId the wanted column id.
     * @return return the wanted columnMetadata or null if not found.
     */
    public ColumnMetadata getById(String wantedId) {
        // defensive programming
        if (wantedId == null) {
            return null;
        }
        for (ColumnMetadata column : columns) {
            if (wantedId.equals(column.getId())) {
                return column;
            }
        }
        return null;
    }

    /**
     * Compute the diff from the given reference to this and update the diffFlag on each columnMetadata.
     * 
     * @param reference the starting point to compute the diff.
     */
    public void diff(RowMetadata reference) {

        // process the new columns
        columns.forEach(column -> {
            if (reference.getById(column.getId()) == null) {
                column.setDiffFlagValue(Flag.NEW.getValue());
            }
        });

        // process the updated columns
        columns.forEach(column -> {
            ColumnMetadata referenceColumn = reference.getById(column.getId());
            if (referenceColumn != null && columnHasChanged(column, referenceColumn)) {
                column.setDiffFlagValue(Flag.UPDATE.getValue());
            }
        });

        // process the deleted columns (add the deleted ones)
        reference.getColumns().forEach(referenceColumn -> {
            if (getById(referenceColumn.getId()) == null) {
                int position = findColumnPosition(reference.getColumns(), referenceColumn.getId());
                referenceColumn.setDiffFlagValue(Flag.DELETE.getValue());
                columns.add(position, referenceColumn);
            }
        });

    }

    /**
     * Change detection between column and its reference (before the transformation)
     * 
     * @param column The column metadata
     * @param reference The column reference
     * @return True if the name, domain or type has changed
     */
    private boolean columnHasChanged(final ColumnMetadata column, final ColumnMetadata reference) {
        return !column.getName().equals(reference.getName()) || !column.getDomain().equals(reference.getDomain())
                || !column.getType().equals(reference.getType());
    }

    /**
     * Return the column position within the given columns.
     *
     * @param columns the list of columns to search the column from.
     * @param colId the wanted column id.
     * @return the column position within the given columns.
     */
    private int findColumnPosition(List<ColumnMetadata> columns, String colId) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().equals(colId)) {
                return i;
            }
        }
        return columns.size();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "RowMetadata{" + "columns=" + columns + '}';
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RowMetadata that = (RowMetadata) o;
        return Objects.equals(columns, that.columns);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(columns);
    }

    /**
     * Insert a new column in this metadata right after the existing <code>columnId</code>. If no column with
     * <code>columnId</code> is to be found, append new column at the end of this row's columns.
     * 
     * @param columnId A non null column id. Empty string is allowed, in this case, column will be appended at the end
     * of existing columns.
     * @param column A non null column to insert in this row's metadata.
     * @return The column id of the newly inserted column.
     */
    public String insertAfter(@Nonnull String columnId, @Nonnull ColumnMetadata column) {
        int insertIndex = 0;
        for (ColumnMetadata columnMetadata : columns) {
            insertIndex++;
            if (columnId.equals(columnMetadata.getId())) {
                break;
            }
        }
        addColumn(column, insertIndex);
        return column.getId();
    }

    /**
     * @see Object#clone()
     */
    @Override
    public RowMetadata clone() {
        // also copy the columns !
        List<ColumnMetadata> copyColumns = new ArrayList<>(columns.size());
        columns.forEach(col -> copyColumns.add(ColumnMetadata.Builder.column().copy(col).build()));
        return new RowMetadata(new ArrayList<>(copyColumns));
    }
}
