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

package org.talend.dataprep.transformation.actions.fillinvalid;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.fill.FillIfEmpty;
import org.talend.dataprep.transformation.actions.fill.FillInvalid;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Unit test for the FillWithStringIfInvalid action.
 *
 * @see FillInvalid
 */
public class FillWithStringIfInvalidTest extends AbstractMetadataBaseTest<FillInvalid> {

    public FillWithStringIfInvalidTest(){
        super(new FillInvalid());
    }

    @PostConstruct
    public void init() {
        action = (FillInvalid) action.adapt(ColumnMetadata.Builder.column().type(Type.STRING).build());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void should_fill_non_valid_string() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "100");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0002");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0002").setType(Type.STRING.getName());

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidStringAction.json"));
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        final RunnableAction runnableAction = factory.create(action, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        runnableAction.getRowAction().apply(row, context);

        // then
        assertEquals("beer", row.get("0002"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_fill_non_valid_string() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "wine");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0002").setType(Type.STRING.getName());

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidStringAction.json"));
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        final RunnableAction runnableAction = factory.create(action, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        runnableAction.getRowAction().apply(row, context);

        // then
        assertEquals("wine", row.get("0002"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_fill_empty_string_other_column() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "DC");
        values.put("0002", "Something");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.STRING.getName());
        rowMetadata.getById("0002").setType(Type.STRING.getName());

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillInvalidStringAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0002");
        final RunnableAction runnableAction = factory.create(action, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        runnableAction.getRowAction().apply(row, context);

        // then
        Assert.assertEquals("Something", row.get("0001"));
        Assert.assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
