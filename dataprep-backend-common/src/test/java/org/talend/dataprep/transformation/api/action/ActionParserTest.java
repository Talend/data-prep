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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;

/**
 * Unit test for the ActionParser class.
 * @see ActionParser
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ActionParserTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public class ActionParserTest {

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

        ParsedActions actualActions = actionParser.parse("");

        // when
        List<DataSetRowAction> rowTransformers = actualActions.getRowTransformers();
        final DataSetRowAction[] actions = rowTransformers.toArray(new DataSetRowAction[rowTransformers.size()]);
        ActionTestWorkbench.test(actualRow, actions);

        // then
        assertEquals(expectedRow, actualRow);
        assertEquals(expectedMetadata, actualRow.getRowMetadata());
    }

    @Test
    public void should_return_expected_actions() throws IOException {
        // given
        DataSetRow actualRow = getDataSetRow();

        String json = IOUtils.toString(ActionParserTest.class.getResourceAsStream("actions.json"));
        ParsedActions actualActions = actionParser.parse(json);

        // when
        List<DataSetRowAction> rowTransformers = actualActions.getRowTransformers();
        final DataSetRowAction[] actions = rowTransformers.toArray(new DataSetRowAction[rowTransformers.size()]);
        ActionTestWorkbench.test(actualRow, actions);

        // then
        RowMetadata expectedMetadata = getRowMetadata();
        expectedMetadata.getById("0001").setName("blah blah blah");
        assertEquals(expectedMetadata, actualRow.getRowMetadata());

        DataSetRow expectedRow = getDataSetRow();
        expectedRow.set("0000", "TOTO");
        assertEquals(expectedRow, actualRow);
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
