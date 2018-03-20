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

package org.talend.dataprep.transformation.api.action.context;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the action context.
 */
public class ActionContextTest {

    /** The context to test. */
    private ActionContext context;

    private RowMetadata row;

    private TransformationContext parent;

    @Before
    public void setUp() throws Exception {
        parent = new TransformationContext();
        row = new RowMetadata();
        context = new ActionContext(parent, row);
    }

    @Test
    public void testParent() {
        assertThat(context.getParent(), is(parent));
    }

    @Test
    public void testColumnCreate() {
        final String column = context.column("test",
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                }
        );
        assertThat(column, is("0000"));
    }

    @Test
    public void testColumnCache() {
        final String column = context.column("test",
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                });
        assertThat(column, is("0000"));
        // Calling twice context with same key shall return same column id
        final String cachedColumn = context.column("test",
                (row) -> {
                    final ColumnMetadata c = column().name("test").type(Type.STRING).build();
                    row.insertAfter("", c);
                    return c;
                }
        );
        assertThat(cachedColumn, is("0000"));
    }

    @Test
    public void testEvict() {
        String key = "key";
        String value = "my value";
        AtomicBoolean valueGenerated = new AtomicBoolean(false);
        Function<Map<String, String>, String> generationCode = m -> {
            valueGenerated.set(true);
            return value;
        };
        context.get(key, generationCode);

        // Value has been generated
        assertTrue(valueGenerated.get());

        valueGenerated.set(false);
        context.get(key, generationCode);

        // Value is not generated again, cache works
        assertFalse(valueGenerated.get());

        context.evict(key);
        context.get(key, generationCode);

        // evict makes the generation work again
        assertTrue(valueGenerated.get());
    }

    @Test
    public void testTwiceSameColumnName() {
        final Function<RowMetadata, ColumnMetadata> creation = (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        };
        // Create a first column with key "test1"
        final String column1 = context.column("test1", creation);
        assertThat(column1, is("0000"));
        assertThat(row.getById("0000").getName(), is("test"));
        // Even though columns share same names, key to obtain them differ, hence the different ids.
        final String column2 = context.column("test2", creation);
        assertThat(column2, is("0001"));
        assertThat(row.getById("0001").getName(), is("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAsImmutable() {
        testColumnCreate(); // Run test column create to get a "test" column.
        final ActionContext immutable = context.asImmutable();
        // Previously created column should still be accessible
        final String cachedColumn = context.column("test", (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        });
        assertThat(cachedColumn, is("0000"));
        // But impossible to add a new column -> UnsupportedOperationException
        immutable.column("test_immutable", (row) -> {
            final ColumnMetadata c = column().name("test").type(Type.STRING).build();
            row.insertAfter("", c);
            return c;
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCreate() {
        context.column("test", (r) -> null);
    }

    @Test
    public void testGetCreate() {

        Object actual = context.get("test", (p) -> "new object");

        assertThat(actual, is("new object"));
    }

    @Test
    public void testGetCache() {

        Object toTheCache = context.get("testCache", (p) -> new BigInteger("123"));
        assertThat(toTheCache, is(new BigInteger("123")));

        // the second call has another supplier that fails the test
        Object actual = context.get("testCache", (p) -> {
            fail("should not be invoked");
            return new BigInteger("123");
        });
        assertThat(actual, is(new BigInteger("123")));
    }

}
