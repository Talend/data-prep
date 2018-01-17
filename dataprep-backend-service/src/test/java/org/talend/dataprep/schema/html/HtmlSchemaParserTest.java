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

package org.talend.dataprep.schema.html;

import java.io.InputStream;
import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.UnsupportedFormatFamily;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HtmlSchemaParserTest extends AbstractSchemaTestUtils {

    private final static Logger logger = LoggerFactory.getLogger(HtmlSchemaParserTest.class);

    private static final String HEADER_SELECTOR = "html body table tr th";

    private static final String VALUES_SELECTOR = "html body table tr td";

    @Autowired
    private HtmlSchemaParser parser;

    @Autowired
    private HtmlSerializer serializer;

    @Autowired
    private HtmlFormatFamily htmlFormatFamily;

    @Autowired
    private UnsupportedFormatFamily unsupportedFormatFamily;

    @Test
    public void read_html_TDP_1136() throws Exception {

        try (InputStream inputStream = this.getClass().getResourceAsStream("sales-force.xls")) {
            // We do know the format and therefore we go directly to the HTML schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#1");
            request.getMetadata().setEncoding("UTF-16");

            Schema result = parser.parse(request);
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getSheetContents()).isNotNull().isNotEmpty().hasSize(1);
            List<ColumnMetadata> columnMetadatas = result.getSheetContents().get(0).getColumnMetadatas();
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(7);

            Assertions.assertThat(columnMetadatas.get(0)) //
                    .isEqualToComparingOnlyGivenFields(
                            ColumnMetadata.Builder.column() //
                                    .type(Type.STRING).id(0).name("UID").build(), //
                            "id", "name", "type");

            Assertions.assertThat(columnMetadatas.get(1)) //
                    .isEqualToComparingOnlyGivenFields(
                            ColumnMetadata.Builder.column() //
                                    .type(Type.STRING).id(1).name("Team Member: Name").build(), //
                            "id", "name", "type");

            Assertions.assertThat(columnMetadatas.get(2)) //
                    .isEqualToComparingOnlyGivenFields(
                            ColumnMetadata.Builder.column() //
                                    .type(Type.STRING).id(2).name("Country").build(), //
                            "id", "name", "type");
        }
    }

    @Test
    public void should_not_accept_csv_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("toto").formatFamilyId("formatGuess#csv").build();
        assertFalse(parser.accept(metadata));
    }

    @Test
    public void should_not_accept_xls_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#xls").build();
        assertFalse(parser.accept(metadata));
    }

    @Test
    public void should_not_accept_html_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#html").build();
        assertFalse(parser.accept(metadata));
    }

}
