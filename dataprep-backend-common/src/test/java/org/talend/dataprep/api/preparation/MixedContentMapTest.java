package org.talend.dataprep.api.preparation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class MixedContentMapTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.registerModule(new MixedContentMapModule());
    }

    @Test
    public void testRead() throws Exception {
        final ObjectReader reader = mapper.reader(MixedContentMap.class);
        final MixedContentMap map = reader.readValue(MixedContentMapTest.class.getResourceAsStream("mixedMapContent.json"));
        assertThat(map, notNullValue());
        assertThat(map.get("string"), is("string value"));
        assertThat(map.get("numeric"), is("10"));
        assertThat(map.get("boolean"), is("true"));
        assertThat(map.get("double"), is("10.1"));
        final String object = map.get("object");
        assertThat(object, sameJSONAs("{\"eq\": { \"field\": \"nbCommands\",\"value\": \"13\" }}"));
    }

    @Test
    public void testWrite() throws Exception {
        MixedContentMap map = new MixedContentMap();
        map.put("string", "string value");
        map.put("numeric", "10");
        map.put("object", "{\"eq\": { \"field\": \"nbCommands\",\"value\": \"13\" }}");
        final StringWriter writer = new StringWriter();
        mapper.writer().writeValue(writer, map);
        final InputStream expected = MixedContentMapTest.class.getResourceAsStream("mixedMapWrite_expected.json");
        assertThat(writer.toString(), sameJSONAsFile(expected));
    }

}