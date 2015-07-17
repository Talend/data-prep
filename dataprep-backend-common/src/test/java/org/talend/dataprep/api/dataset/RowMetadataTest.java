package org.talend.dataprep.api.dataset;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.talend.dataprep.api.dataset.diff.Flag;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the RowMetadata class.
 * 
 * @see RowMetadata
 */
public class RowMetadataTest {

    @Test
    public void no_diff() {
        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("0", "toto"));
        columns.add(getColumnMetadata("1", "tata"));
        columns.add(getColumnMetadata("2", "tutu"));
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
        columns.add(getColumnMetadata("0", "toto"));
        RowMetadata reference = new RowMetadata(columns);

        // when
        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(getColumnMetadata("1", "tata")); // new column
        expected.add(getColumnMetadata("2", "titi")); // new column
        List<ColumnMetadata> temp = new ArrayList<>();
        temp.addAll(columns);
        temp.addAll(expected);
        RowMetadata row = new RowMetadata(temp);
        row.diff(reference);

        // then (collect the columns with the new flag only and compare them with
        // the expected result)
        List<ColumnMetadata> actual = row.getColumns().stream() //
                .filter(column -> Flag.NEW.getValue().equals(column.getDiffFlagValue())) //
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @Test
    public void should_diff_deleted_columns() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("0", "toto"));

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(getColumnMetadata("1", "tata"));
        expected.add(getColumnMetadata("2", "titi"));

        List<ColumnMetadata> temp = new ArrayList<>();
        temp.addAll(columns);
        temp.addAll(expected);
        RowMetadata reference = new RowMetadata(temp);

        // when
        RowMetadata row = new RowMetadata(columns); // deleted columns 1 & 2
        row.diff(reference);

        // then (collect the columns with the new flag only and compare them with
        // the expected result)
        List<ColumnMetadata> actual = row.getColumns().stream() //
                .filter(column -> Flag.DELETE.getValue().equals(column.getDiffFlagValue())) //
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @Test
    public void should_diff_updated_columns() {

        // given
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(getColumnMetadata("0", "toto"));

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(getColumnMetadata("1", "tata"));
        expected.add(getColumnMetadata("2", "titi"));

        List<ColumnMetadata> temp = new ArrayList<>();
        temp.addAll(columns);
        temp.addAll(expected);
        RowMetadata reference = new RowMetadata(temp);

        // when
        temp = new ArrayList<>();
        temp.addAll(columns);
        temp.add(getColumnMetadata("1", "new tata name")); // updated name
        temp.add(getColumnMetadata("2", "new titi name")); // updated name
        RowMetadata row = new RowMetadata(temp);
        row.diff(reference);

        // then (collect the columns with the new flag only and compare them with
        // the expected result)
        List<ColumnMetadata> actual = row.getColumns().stream() //
                .filter(column -> Flag.UPDATE.getValue().equals(column.getDiffFlagValue())) //
                .collect(Collectors.toList());

        assertEquals(actual.size(), 2);
        assertTrue(actual.get(0).getId().equals("1"));
        assertTrue(actual.get(1).getId().equals("2"));

    }

    /**
     * @param id the column id.
     * @param name the column name.
     * @return a new column.
     */
    private ColumnMetadata getColumnMetadata(String id, String name) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(Type.STRING).build();
    }
}
