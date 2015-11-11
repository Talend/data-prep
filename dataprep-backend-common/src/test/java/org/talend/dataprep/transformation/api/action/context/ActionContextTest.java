package org.talend.dataprep.transformation.api.action.context;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;

public class ActionContextTest {

    private ActionContext context;

    private RowMetadata row;

    private TransformationContext parent;

    @Before
    public void setUp() throws Exception {
        parent = new TransformationContext();
        row = new RowMetadata();
        context = new ActionContext(parent);
    }

    @Test
    public void testParent() throws Exception {
        assertThat(context.getParent(), is(parent));
    }

    @Test
    public void testColumnCreate() throws Exception {
        final String column = context.column("test",
                row,
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                }
        );
        assertThat(column, is("0000"));
    }

    @Test
    public void testColumnCache() throws Exception {
        final String column = context.column("test",
                row,
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                });
        assertThat(column, is("0000"));
        // Calling twice context with same key shall return same column id
        final String cachedColumn = context.column("test",
                row,
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                }
        );
        assertThat(cachedColumn, is("0000"));
    }

    @Test
    public void testTwiceSameColumnName() throws Exception {
        final Function<RowMetadata, ColumnMetadata> creation = (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        };
        // Create a first column with key "test1"
        final String column1 = context.column("test1", row, creation);
        assertThat(column1, is("0000"));
        assertThat(row.getById("0000").getName(), is("test"));
        // Even though columns share same names, key to obtain them differ, hence the different ids.
        final String column2 = context.column("test2", row, creation);
        assertThat(column2, is("0001"));
        assertThat(row.getById("0001").getName(), is("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAsImmutable() throws Exception {
        testColumnCreate(); // Run test column create to get a "test" column.
        final ActionContext immutable = context.asImmutable();
        // Previously created column should still be accessible
        final String cachedColumn = context.column("test", row, (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        });
        assertThat(cachedColumn, is("0000"));
        // But impossible to add a new column -> UnsupportedOperationException
        immutable.column("test_immutable", row, (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCreate() throws Exception {
        context.column("test", row, (r) -> null);
    }

}