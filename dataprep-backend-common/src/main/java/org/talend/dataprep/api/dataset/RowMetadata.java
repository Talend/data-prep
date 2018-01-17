// ============================================================================
//
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

package org.talend.dataprep.api.dataset;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.json.ColumnContextDeserializer;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Models metadata information for a row of a data set.
 * More of a DI Schema Metadata albeit it contains indirectly data quality reports and statistics from its dataset.
 */
public class RowMetadata implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RowMetadata.class);

    private static final String COLUMN_ID_PATTERN = "0000";

    /** List of row metadata. */
    @JsonProperty("columns")
    @JsonDeserialize(using = ColumnContextDeserializer.class)
    private final List<ColumnMetadata> columns = new ArrayList<>();

    @JsonProperty("nextId")
    private int nextId = 0;

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
        return Collections.unmodifiableList(columns);
    }

    /**
     * Returns true if this data set metadata is compatible with <tt>rowMetadata</tt> (they have same types in
     * the same order and same column names) and false otherwise.
     *
     * @param other the specified row metadata
     * @return true if this row metadata is compatible with the specified one and false otherwise
     */
    public boolean compatible(RowMetadata other) {
        if (other == null || columns.size() != other.getColumns().size()) {
            return false;
        }
        int size = columns.size();
        List<ColumnMetadata> otherColumns = other.getColumns();
        for (int i = 0; i < size; i++) {
            if (!columns.get(i).compatible(otherColumns.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param columnMetadata the metadata to set.
     */
    public void setColumns(List<ColumnMetadata> columnMetadata) {
        columns.clear();
        nextId = 0;
        columnMetadata.forEach(this::addColumn);
    }

    public ColumnMetadata addColumn(ColumnMetadata columnMetadata) {
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
        String columnIdFromMetadata = columnMetadata.getId();
        DecimalFormat columnIdFormat = new DecimalFormat(COLUMN_ID_PATTERN);
        if (StringUtils.isBlank(columnIdFromMetadata)) {
            columnMetadata.setId(columnIdFormat.format(nextId));
            nextId++;
        } else {
            try {
                int columnId = columnIdFormat.parse(columnIdFromMetadata).intValue();
                int possibleNextId = columnId + 1;
                if (possibleNextId > nextId) {
                    nextId = possibleNextId;
                }
            } catch (ParseException e) {
                LOGGER.error("Unable to parse column id from metadata '" + columnIdFromMetadata + "'", e);
            }
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
        return !Objects.equals(column.getName(), reference.getName()) //
                || !Objects.equals(column.getDomain(), reference.getDomain()) //
                || !Objects.equals(column.getType(), reference.getType());
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

    @Override
    public String toString() {
        return "RowMetadata{" + "columns=" + columns + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !getClass().isInstance(o))
            return false;
        RowMetadata that = (RowMetadata) o;
        return Objects.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns);
    }

    public void update(@Nonnull String columnId, @Nonnull ColumnMetadata column) {
        if (getById(columnId) == null) {
            return;
        }
        int updatePos = 0;
        for (ColumnMetadata columnMetadata : columns) {
            if (columnId.equals(columnMetadata.getId())) {
                break;
            }
            updatePos++;
        }
        columns.set(updatePos, column);
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

    @Override
    public RowMetadata clone() {
        // also copy the columns !
        List<ColumnMetadata> copyColumns = new ArrayList<>(columns.size());
        columns.forEach(col -> copyColumns.add(ColumnMetadata.Builder.column().copy(col).build()));
        final RowMetadata clone = new RowMetadata(new ArrayList<>(copyColumns));
        clone.nextId = nextId;
        return clone;
    }

    public Schema toSchema() {
        return RowMetadataUtils.toSchema(this);
    }
}
