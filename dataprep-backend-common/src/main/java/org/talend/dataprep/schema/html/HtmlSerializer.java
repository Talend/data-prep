package org.talend.dataprep.schema.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.Serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Service("serializer#html")
public class HtmlSerializer implements Serializer {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(HtmlSerializer.class);

    @Override
    public InputStream serialize(InputStream rawContent, DataSetMetadata metadata) {

        try {

            Map<String, String> parameters = metadata.getContent().getParameters();
            String encoding = metadata.getEncoding();
            String valuesSelector = parameters.get(HtmlFormatGuesser.VALUES_SELECTOR_KEY);

            StringWriter writer = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(writer);

            generator.writeStartArray();

            List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();

            // select values
            String str = IOUtils.toString(rawContent, encoding);

            Document document = Jsoup.parse(str);

            Elements values = document.select(valuesSelector);

            // selector returns all found cells so we need to swich over new lines with a columns index
            int idx = 0;

            for (Element value : values) {

                if (idx == 0) {
                    generator.writeStartObject();
                }
                ColumnMetadata columnMetadata = columns.get(idx);
                generator.writeFieldName(columnMetadata.getId());
                String cellValue = value.text();
                if (cellValue != null) {
                    generator.writeString(cellValue);
                } else {
                    generator.writeNull();
                }
                idx++;
                if (idx >= columns.size()) {
                    idx = 0;
                    generator.writeEndObject();
                }
            }

            generator.writeEndArray();
            generator.flush();
            return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }

    }
}
