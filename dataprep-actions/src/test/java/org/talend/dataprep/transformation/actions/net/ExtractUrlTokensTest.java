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

package org.talend.dataprep.transformation.actions.net;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Test class for ExtractUrlTokens action. Creates one consumer, and test it.
 *
 * @see ExtractUrlTokens
 */
public class ExtractUrlTokensTest extends AbstractMetadataBaseTest<ExtractUrlTokens> {

    private Map<String, String> parameters;

    public ExtractUrlTokensTest() {
        super(new ExtractUrlTokens());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(ExtractUrlTokensTest.class.getResourceAsStream("extractUrlTokensAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.SPLIT.getDisplayName(Locale.US)));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "http://www.talend.com");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "http://www.talend.com");
        expectedValues.put("0003", "http");
        expectedValues.put("0004", "www.talend.com");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "");
        expectedValues.put("0009", "");
        expectedValues.put("0010", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.order().values());
    }

    @Test
    public void test_values_port_as_int() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "http://stef:pwd@10.42.10.99:80/home/datasets?datasetid=c522a037-7bd8-42c1-a8ee-a0628c66d8c4#frag");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001",
                "http://stef:pwd@10.42.10.99:80/home/datasets?datasetid=c522a037-7bd8-42c1-a8ee-a0628c66d8c4#frag");
        expectedValues.put("0003", "http");
        expectedValues.put("0004", "10.42.10.99");
        expectedValues.put("0005", "80");
        expectedValues.put("0006", "/home/datasets");
        expectedValues.put("0007", "datasetid=c522a037-7bd8-42c1-a8ee-a0628c66d8c4");
        expectedValues.put("0008", "frag");
        expectedValues.put("0009", "stef");
        expectedValues.put("0010", "pwd");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_invalid_values() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "http_www.talend.com");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "http_www.talend.com");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "http_www.talend.com");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "");
        expectedValues.put("0009", "");
        expectedValues.put("0010", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.order().values());
    }

    @Test
    public void test_empty_values() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "");
        expectedValues.put("0009", "");
        expectedValues.put("0010", "");

        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.order().values());
    }

    @Test
    public void test_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "url"));
        input.add(createMetadata("0002", "last update"));
        final DataSetRow row = new DataSetRow(new RowMetadata(input));

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "url"));
        expected.add(createMetadata("0003", "url_protocol"));
        expected.add(createMetadata("0004", "url_host"));
        expected.add(createMetadata("0005", "url_port", Type.INTEGER));
        expected.add(createMetadata("0006", "url_path"));
        expected.add(createMetadata("0007", "url_query"));
        expected.add(createMetadata("0008", "url_fragment"));
        expected.add(createMetadata("0009", "url_user"));
        expected.add(createMetadata("0010", "url_password"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    @Override
    protected ColumnMetadata.Builder columnBaseBuilder() {
        return super.columnBaseBuilder().headerSize(12).valid(5).invalid(2).empty(0);
    }

    @Test
    public void should_accept_column() {
        ColumnMetadata column = getColumn(Type.STRING);
        column.setDomain("url");
        assertTrue(action.acceptField(column));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
        assertFalse(action.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));

        ColumnMetadata column = getColumn(Type.STRING);
        column.setDomain("not an url");
        assertFalse(action.acceptField(column));
    }

    @Test
    public void testApplyNonUtf8HostsExtraction_TDQ_14551() {
        // TDQ-14551: Support URLs with Asian characters
        assertEquals("例子.卷筒纸", testApply("https://user:pass@例子.卷筒纸").hostToken);
        assertEquals("例子.卷筒纸", testApply("https://例子.卷筒纸").hostToken);
        assertEquals("引き割り.引き割り", testApply("https://引き割り.引き割り:8080/引き割metadata.html").hostToken);
        assertEquals("引き割り.引き割り", testApply("https://user:pass@引き割り.引き割り:8080/引き割metadata.html").hostToken);
    }

    @Test
    public void testApplyChineseHostExtraction() {
        TokenExtractionResult result = testApply("https://user:pass@例子.卷筒纸:8580");
        assertEquals("user", result.userToken);
        assertEquals("pass", result.passwordToken);
        assertEquals("8580", result.portToken);
    }

    @Test
    public void testApplyChineseHostNoPasswordExtraction() {
        TokenExtractionResult result = testApply("https://user@例子.卷筒纸:8580/home/datasets?datasetid=c522a037-7bd8-42c1-a8ee-a0628c66d8c4#frag");
        assertEquals("user", result.userToken);
        assertEquals("", result.passwordToken);
    }

    @Test
    public void testApplyJapaneseHostExtraction() {
        TokenExtractionResult result = testApply("https://user:pass@引き割り.引き割り:8580");
        assertEquals("user", result.userToken);
        assertEquals("pass", result.passwordToken);
        assertEquals("8580", result.portToken);
    }

    @Test
    public void testApplyChineseAndJapaneseMixTokenExtraction() {
        TokenExtractionResult result = testApply("https://卷筒:纸@引き割り.引き割り:8580");
        assertEquals("卷筒", result.userToken);
        assertEquals("纸", result.passwordToken);
    }

    @Test
    @Ignore
    public void testApplyEncodedCharactersTokenExtraction() throws Exception {
        // encoded version of: https://卷筒:纸@引き割り.引き割り:8580
        TokenExtractionResult result = testApply("https://%E5%8D%B7%E7%AD%92:%E7%BA%B8@%E5%BC%95%E3%81%8D%E5%89%B2%E3%82%8A.%E5%BC%95%E3%81%8D%E5%89%B2%E3%82%8A:8580");

        assertEquals("%E5%BC%95%E3%81%8D%E5%89%B2%E3%82%8A.%E5%BC%95%E3%81%8D%E5%89%B2%E3%82%8A", result.hostToken);
        assertEquals("%E5%8D%B7%E7%AD%92", result.userToken);
        assertEquals("%E7%BA%B8", result.passwordToken);
        assertEquals("8580", result.portToken);
    }

    @Test
    public void testProtocolExtractor_TDQ_14551() {
        assertEquals("http", testApply("http://www.yahoo.fr").protocolToken);
        assertEquals("mailto", testApply("mailto:smallet@talend.com").protocolToken);
        assertEquals("ftp", testApply("ftp://server:21/this/is/a/resource").protocolToken);
        assertEquals("http", testApply("HTTP://www.yahoo.fr").protocolToken);
        assertEquals("http", testApply("http:10.42.10.99:80/home/datasets?datasetid=c522a037").protocolToken);
        assertEquals("file", testApply("file://server:21/this/is/a/resource").protocolToken);
        assertEquals("mvn", testApply("mvn://server:21/this/is/a/resource").protocolToken);
        assertEquals("tagada", testApply("tagada://server:21/this/is/a/resource").protocolToken);
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    // Utility to test the action
    private TokenExtractionResult testApply(String myUrl) {
        // given
        Map<String, String> parameters = new HashMap<>();
        parameters.put("column_id", "0000");
        parameters.put("column_name", "url");
        parameters.put("scope", "column");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", myUrl);
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        TokenExtractionResult result = new TokenExtractionResult();
        result.protocolToken = row.get("0001");
        result.hostToken = row.get("0002");
        result.portToken = row.get("0003");
        result.pathToken = row.get("0004");
        result.queryToken = row.get("0005");
        result.fragmentToken = row.get("0006");
        result.userToken = row.get("0007");
        result.passwordToken = row.get("0008");
        return result;
    }

    private static class TokenExtractionResult {
        private String protocolToken;
        private String hostToken;
        private String portToken;
        private String pathToken;
        private String queryToken;
        private String fragmentToken;
        private String userToken;
        private String passwordToken;
    }

}
