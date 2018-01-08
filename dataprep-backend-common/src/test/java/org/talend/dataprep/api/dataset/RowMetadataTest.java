//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.row.Flag.UPDATE;
import static org.talend.dataprep.test.SerializableMatcher.isSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.api.type.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the RowMetadata class.
 *
 * @see RowMetadata
 */
public class RowMetadataTest {

    @Test
    public void should_create_new_column_at_end() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto", 0));
        columns.add(getColumnMetadata("titi", 1));
        columns.add(getColumnMetadata("tutu", 2));
        RowMetadata reference = new RowMetadata(columns);

        // when
        ColumnMetadata newColumnMetadata1 = getColumnMetadata("tata"); // new column
        reference.insertAfter("", newColumnMetadata1);

        // then
        List<ColumnMetadata> columnsReference = reference.getColumns();

        ColumnMetadata col0003 = columnsReference.get(3);
        assertEquals(col0003.getId(), "0003"); //
        assertEquals(col0003.getName(), "tata"); // tata (new)
    }

    @Test
    public void should_create_new_column_next_to_one_after_deleted_another() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto"));
        columns.add(getColumnMetadata("titi", 2));
        columns.add(getColumnMetadata("tutu", 3));
        RowMetadata reference = new RowMetadata(columns);

        // when
        ColumnMetadata newColumnMetadata1 = getColumnMetadata("tata"); // new column
        reference.insertAfter("0000", newColumnMetadata1);
        ColumnMetadata newColumnMetadata2 = getColumnMetadata("tete"); // new column
        reference.insertAfter("0004", newColumnMetadata2);

        // then
        List<ColumnMetadata> columnsReference = reference.getColumns();

        ColumnMetadata col0000 = columnsReference.get(0);
        assertEquals(col0000.getId(), "0000"); //
        assertEquals(col0000.getName(), "toto"); // toto

        ColumnMetadata col0004 = columnsReference.get(1);
        assertEquals(col0004.getId(), "0004"); //
        assertEquals(col0004.getName(), "tata"); // tata (new)

        ColumnMetadata col0005 = columnsReference.get(2);
        assertEquals(col0005.getId(), "0005"); //
        assertEquals(col0005.getName(), "tete"); // tete (new)

        ColumnMetadata col0002 = columnsReference.get(3);
        assertEquals(col0002.getId(), "0002"); //
        assertEquals(col0002.getName(), "titi"); // titi

        ColumnMetadata col0003 = columnsReference.get(4);
        assertEquals(col0003.getId(), "0003"); //
        assertEquals(col0003.getName(), "tutu"); // tutu
    }

    @Test
    public void no_diff() {
        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto"));
        columns.add(getColumnMetadata("tata"));
        columns.add(getColumnMetadata("tutu"));
        RowMetadata rowMetadata = new RowMetadata(columns);

        // when
        RowMetadata reference = new RowMetadata(columns);
        rowMetadata.diff(reference);

        // then (make sure the diff flag is always null)
        rowMetadata.getColumns().forEach(column -> assertNull(column.getDiffFlagValue()));
    }

    @Test
    public void should_diff_new_columns() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto", 0));
        RowMetadata reference = new RowMetadata(columns);

        // when
        List<ColumnMetadata> rowCols = new ArrayList<>();
        rowCols.add(getColumnMetadata("tata", 1)); // new column
        rowCols.add(reference.getColumns().get(0));
        rowCols.add(getColumnMetadata("titi", 2)); // new column
        RowMetadata row = new RowMetadata(rowCols);

        row.diff(reference);

        // then checks the diff flags as well as the order
        checkColumn(row.getColumns().get(0), "tata", Flag.NEW.getValue()); // tata (new)
        checkColumn(row.getColumns().get(1), "toto", null); // toto
        checkColumn(row.getColumns().get(2), "titi", Flag.NEW.getValue()); // titi (new)
    }

    private void checkColumn(ColumnMetadata column, String name, String flag) {
        assertEquals(column.getName(), name);
        assertEquals(column.getDiffFlagValue(), flag);
    }

    @Test
    public void should_diff_deleted_columns() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto", 1));

        List<ColumnMetadata> cols = new ArrayList<>();
        cols.add(getColumnMetadata("tata", 0));
        cols.add(columns.get(0));
        cols.add(getColumnMetadata("titi", 2));
        RowMetadata reference = new RowMetadata(cols);

        // when
        RowMetadata row = new RowMetadata(columns); // deleted columns 1 & 2
        row.diff(reference);

        // then checks the diff flags as well as the order
        checkColumn(row.getColumns().get(0), "tata", Flag.DELETE.getValue()); // tata (new)
        checkColumn(row.getColumns().get(1), "toto", null); // toto
        checkColumn(row.getColumns().get(2), "titi", Flag.DELETE.getValue()); // titi (new)
    }

    @Test
    public void should_diff_updated_columns_when_name_has_changed() {
        // given
        final List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto"));

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(getColumnMetadata("tata"));
        expected.add(getColumnMetadata("titi"));

        List<ColumnMetadata> temp = new ArrayList<>();
        temp.addAll(columns);
        temp.addAll(expected);
        RowMetadata reference = new RowMetadata(temp);

        temp = new ArrayList<>();
        temp.addAll(columns);
        temp.add(getColumnMetadata("new tata name")); // updated name
        temp.add(getColumnMetadata("new titi name")); // updated name
        RowMetadata row = new RowMetadata(temp);

        // when
        row.diff(reference);

        // then (collect the columns with the new flag only and compare them with
        // the expected result)
        final List<ColumnMetadata> actual = row.getColumns().stream() //
                .filter(column -> UPDATE.getValue().equals(column.getDiffFlagValue())) //
                .collect(Collectors.toList());

        assertEquals(actual.size(), 2);
        assertThat(actual.get(0).getId(), is("0001"));
        assertThat(actual.get(1).getId(), is("0002"));
    }

    @Test
    public void should_diff_updated_columns_when_domain_has_changed() {
        // given
        final ColumnMetadata referenceColumn = getColumnMetadata("toto");
        referenceColumn.setDomain("country");
        final RowMetadata reference = new RowMetadata(singletonList(referenceColumn));

        final ColumnMetadata rowColumn = getColumnMetadata("toto");
        rowColumn.setDomain("firstname");
        final RowMetadata row = new RowMetadata(singletonList(rowColumn));

        // when
        row.diff(reference);

        // then
        assertThat(rowColumn.getDiffFlagValue(), is(UPDATE.getValue()));
    }

    @Test
    public void should_diff_updated_columns_when_type_has_changed() {
        // given
        final ColumnMetadata referenceColumn = getColumnMetadata("toto");
        referenceColumn.setType("string");
        final RowMetadata reference = new RowMetadata(singletonList(referenceColumn));

        final ColumnMetadata rowColumn = getColumnMetadata("toto");
        rowColumn.setType("integer");
        final RowMetadata row = new RowMetadata(singletonList(rowColumn));

        // when
        row.diff(reference);

        // then
        assertThat(rowColumn.getDiffFlagValue(), is(UPDATE.getValue()));
    }

    @Test
    public void clone_should_duplicate_columns() {
        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("toto", 2));
        RowMetadata row = new RowMetadata(columns);

        // when
        RowMetadata clone = row.clone();
        clone.getColumns().get(0).setName("NOT toto");

        // then
        assertEquals(row.getColumns().get(0).getName(), "toto");
    }

    @Test
    public void should_be_compatible() {
        // given
        List<ColumnMetadata> columns1 = new ArrayList<>();
        List<ColumnMetadata> columns2 = new ArrayList<>();
        ColumnMetadata metadata1 = getColumnMetadata("first", 1, Type.STRING);
        ColumnMetadata metadata2 = getColumnMetadata("last", 2, Type.STRING);
        columns1.add(metadata1);
        columns1.add(metadata2);
        columns2.add(metadata1);
        columns2.add(metadata2);
        RowMetadata row1 = new RowMetadata(columns1);
        RowMetadata row2 = new RowMetadata(columns2);

        // when
        RowMetadata clone = row1.clone();

        // then
        assertTrue(row1.compatible(row2));
        assertTrue(row2.compatible(row1));
        assertTrue(row1.compatible(clone));
    }

    @Test
    public void should_be_incompatible() {
        // given
        List<ColumnMetadata> columns1 = new ArrayList<>();
        List<ColumnMetadata> columns2 = new ArrayList<>();
        List<ColumnMetadata> columns3 = new ArrayList<>();
        ColumnMetadata metadata1 = getColumnMetadata("first", 1, Type.STRING);
        ColumnMetadata metadata2 = getColumnMetadata("last", 2, Type.STRING);
        ColumnMetadata metadata3 = getColumnMetadata("last", 2, Type.INTEGER);
        columns1.add(metadata1);
        columns1.add(metadata2);
        columns2.add(metadata1);
        columns2.add(metadata3);
        columns3.add(metadata2);
        columns3.add(metadata1);

        // when
        RowMetadata row1 = new RowMetadata(columns1);
        RowMetadata row2 = new RowMetadata(columns2);
        RowMetadata row3 = new RowMetadata(columns3);
        RowMetadata row4 = null;

        // then
        assertFalse(row1.compatible(row2));
        assertFalse(row2.compatible(row1));
        assertFalse(row3.compatible(row1));
        assertFalse(row2.compatible(row3));
        assertFalse(row2.compatible(row4));
    }

    @Test
    public void shouldReturnTrueForRowMetadata() throws Exception {
        assertThat(RowMetadata.class, isSerializable());
    }

    @Test
    public void shouldSerializeNextId_TDP3927() throws Exception {

        RowMetadata value = new RowMetadata();
        value.addColumn(getColumnMetadata("toto"));
        value.deleteColumnById("0000");

        ObjectMapper mapper = new ObjectMapper();
        String valueAsString = mapper.writeValueAsString(value);
        value = mapper.readValue(valueAsString, RowMetadata.class);

        ColumnMetadata addedColumn = new ColumnMetadata();
        addedColumn.setName("added_column");
        value.addColumn(addedColumn);

        assertEquals("0001", addedColumn.getId());
    }


    /**
     * @param name the column name.
     * @return a new column.
     */
    private ColumnMetadata getColumnMetadata(String name) {
        return ColumnMetadata.Builder.column().name(name).type(Type.STRING).build();
    }

    /**
     * @param name the column name.
     * @param id the column id.
     * @return a new column.
     */
    private ColumnMetadata getColumnMetadata(String name, int id) {
        return ColumnMetadata.Builder.column().name(name).type(Type.STRING).id(id).build();
    }

    /**
     * @param name the column name.
     * @param id the column id.
     * @return a new column.
     */
    private ColumnMetadata getColumnMetadata(String name, int id, Type type) {
        return ColumnMetadata.Builder.column().name(name).type(Type.STRING).id(id).type(type).build();
    }

}
