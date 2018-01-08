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

package org.talend.dataprep.api.preparation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
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
        assertThat(map.get("null"), nullValue());
        assertThat(map.get("empty"), is(""));
        final String object = map.get("object");
        assertThat(object, sameJSONAs("{\"eq\": { \"field\": \"nbCommands\",\"value\": \"13\" }}"));
        final String array = map.get("array");
        assertThat(array, sameJSONAs("[1, 2, 3]"));
    }

    @Test
    public void testWrite() throws Exception {
        MixedContentMap map = new MixedContentMap();
        map.put("string", "string value");
        map.put("numeric", "10");
        map.put("null", null);
        map.put("empty", "");
        map.put("object", "{\"eq\": { \"field\": \"nbCommands\",\"value\": \"13\" }}");
        map.put("array", "[1, 2, 3]");
        map.put("regex1", "[AZaz0-9]*");
        map.put("regex2", "[ ].*");
        final StringWriter writer = new StringWriter();
        mapper.writer().writeValue(writer, map);
        final InputStream expected = MixedContentMapTest.class.getResourceAsStream("mixedMapWrite_expected.json");
        assertThat(writer.toString(), sameJSONAsFile(expected));
    }
}
