//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

/**
 * Unit test for the ActionParser class.
 * @see ActionParser
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActionParserTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
public class ActionParserTest {

    @Autowired
    ActionRegistry actionRegistry;

    /** The bean to test. */
    @Autowired
    private ActionParser actionParser;

    @Test(expected = IllegalArgumentException.class)
    public void should_not_accept_null_actions() {
        actionParser.parse(null);
    }

    @Test(expected = TDPException.class)
    public void should_not_accept_invalid_actions() {
        actionParser.parse("blah blah blah");
    }

    @Test(expected = TDPException.class)
    public void should_not_accept_unknown_actions() throws IOException {
        String json = IOUtils.toString(ActionParserTest.class.getResourceAsStream("unknown_actions.json"));
        actionParser.parse(json);
    }

    @Test
    public void empty_string_should_return_noop_actions() {
        // given
        DataSetRow actualRow = getDataSetRow();
        DataSetRow expectedRow = actualRow.clone();

        RowMetadata expectedMetadata = getRowMetadata();

        List<Action> actualActions = actionParser.parse("");

        // when
        final Action[] actions = actualActions.toArray(new Action[actualActions.size()]);
        ActionTestWorkbench.test(actualRow, actionRegistry, actions);

        // then
        assertEquals(expectedRow, actualRow);
        assertEquals(expectedMetadata, actualRow.getRowMetadata());
    }

    @Test
    public void should_return_expected_actions() throws IOException {
        String json = IOUtils.toString(ActionParserTest.class.getResourceAsStream("actions.json"));

        // when
        List<Action> actualActions = actionParser.parse(json);

        // then
        assertTrue(actualActions.size() == 1);
        Action actionParsed = actualActions.get(0);
        assertEquals("lookup", actionParsed.getName());
    }


    /**
     * @return a default dataset row.
     */
    private DataSetRow getDataSetRow() {
        Map<String, String> values = new HashMap<>();
        values.put("0000", "toto");
        values.put("0001", "123456");
        values.put("0002", "true");
        return new DataSetRow(getRowMetadata(), values);
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
