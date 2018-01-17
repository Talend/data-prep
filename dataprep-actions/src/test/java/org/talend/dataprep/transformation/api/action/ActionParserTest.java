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

package org.talend.dataprep.transformation.api.action;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the ActionParser class.
 */
public class ActionParserTest {

    /** The dataprep ready jackson builder. */
    protected final ObjectMapper mapper = new ObjectMapper();

    protected final ActionFactory factory = new ActionFactory();

    protected final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    private final ActionParser actionParser = new ActionParser(factory, actionRegistry, mapper);

    @Test(expected = IllegalArgumentException.class)
    public void should_not_accept_null_actions() {
        actionParser.parse(null);
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_not_accept_invalid_actions() {
        actionParser.parse("blah blah blah");
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_not_accept_unknown_actions() throws IOException {
        String json = IOUtils.toString(ActionParserTest.class.getResourceAsStream("unknown_actions.json"),
                UTF_8);
        actionParser.parse(json);
    }

    @Test
    public void empty_string_should_return_noop_actions() {
        // given
        DataSetRow actualRow = getDataSetRow();
        DataSetRow expectedRow = actualRow.clone();

        RowMetadata expectedMetadata = getRowMetadata();

        List<RunnableAction> actualActions = actionParser.parse("");

        // when
        final RunnableAction[] actions = actualActions.toArray(new RunnableAction[actualActions.size()]);
        ActionTestWorkbench.test(actualRow, actionRegistry, actions);

        // then
        assertEquals(expectedRow, actualRow);
        assertEquals(expectedMetadata, actualRow.getRowMetadata());
    }

    @Test
    public void should_return_expected_actions() throws IOException {
        String json = IOUtils.toString(ActionParserTest.class.getResourceAsStream("actions.json"), UTF_8);

        // when
        List<RunnableAction> actualActions = actionParser.parse(json);

        // then
        assertTrue(actualActions.size() == 1);
        Action actionParsed = actualActions.get(0);
        assertEquals("lookup", actionParsed.getName());
    }

    /**
     * @return a default dataset row.
     */
    private DataSetRow getDataSetRow() {
        return builder() //
                .with(value("toto").type(Type.STRING).name("name")) //
                .with(value("123456").type(Type.INTEGER).name("count")) //
                .with(value("true").type(Type.BOOLEAN).name("default")) //
                .build();
    }

    /**
     * @return a default RowMetadata.
     */
    private RowMetadata getRowMetadata() {
        List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).build());
        columns.add(ColumnMetadata.Builder.column().id(1).name("count").type(Type.INTEGER).build());
        columns.add(ColumnMetadata.Builder.column().id(2).name("default").type(Type.BOOLEAN).build());
        return new RowMetadata(columns);
    }
}
