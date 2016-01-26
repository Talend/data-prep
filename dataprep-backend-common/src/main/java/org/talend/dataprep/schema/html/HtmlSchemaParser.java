package org.talend.dataprep.schema.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

/**
 * This class is in charge of parsing html file (note jsoup is used see http://jsoup.org/ )
 */
@Service("parser#html")
public class HtmlSchemaParser implements SchemaParser {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlSchemaParser.class);

    @Inject
    private HtmlFormatGuesser htmlFormatGuesser;

    /**
     * @see SchemaParser#parse(Request)
     */
    @Override
    public SchemaParserResult parse(Request request) {

        Map<String, String> parameters = request.getMetadata().getContent().getParameters();
        String encoding = request.getMetadata().getEncoding();
        String headerSelector = parameters.get(HtmlFormatGuesser.HEADER_SELECTOR_KEY);
        String valuesSelector = parameters.get(HtmlFormatGuesser.VALUES_SELECTOR_KEY);

        try {
            String str = IOUtils.toString(request.getContent(), encoding);

            Document document = Jsoup.parse(str);

            Elements headers = document.select(headerSelector);

            List<ColumnMetadata> columnMetadatas = new ArrayList<>();

            int id = 0;

            for (Element header : headers) {
                LOGGER.debug("header: {}", header);
                columnMetadatas.add(ColumnMetadata.Builder.column() //
                        .type(Type.STRING) // TODO ? ATM not doing any complicated type calculation
                        .name(header.text()) //
                        .id(id) //
                        .build());
                id++;
            }

            Elements values = document.select(valuesSelector);

            for (Element value : values) {
                LOGGER.debug("value: {}", value);
            }

            SchemaParserResult.SheetContent sheetContent = new SchemaParserResult.SheetContent();
            sheetContent.setColumnMetadatas(columnMetadatas);

            return SchemaParserResult.Builder.parserResult() //
                    .sheetContents(Collections.singletonList(sheetContent)) //
                    .draft(false) //
                    .build();

        } catch (Exception e) {
            LOGGER.debug("Exception during parsing html request :" + e.getMessage(), e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

    }

}
