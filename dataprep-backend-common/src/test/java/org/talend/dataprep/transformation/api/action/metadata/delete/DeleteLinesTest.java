package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DeleteLinesTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
public class DeleteLinesTest {

    @Autowired
    private DeleteLines action;

    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        parameters = ActionMetadataTestUtils.parseParameters( //
                DeleteLinesTest.class.getResourceAsStream("deleteLines.json"));
    }

    @Test
    public void should_accept_column() {
        final List<Type> allTypes = Type.ANY.list();
        for (Type type : allTypes) {
            assertTrue(action.acceptColumn(getColumn(type)));
        }
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.FILTERED.getDisplayName()));
    }

    @Test
    public void should_delete() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "Berlin");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_not_delete() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "Paris");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }
}