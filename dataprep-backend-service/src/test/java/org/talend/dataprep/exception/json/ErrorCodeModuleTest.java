package org.talend.dataprep.exception.json;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.exception.ErrorCodeDto;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorCodeModuleTest {

    @Test
    public void directSerializationOfErrorCodeShouldWork() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ErrorCodeModule());
        String s = mapper.writeValueAsString(new ErrorCodeDto()
                .setGroup("GROUP")
                .setProduct("PRODUCT")
                .setCode("CODE")
                .setHttpStatus(123)
                .setExpectedContextEntries(singletonList("entry")));

        ErrorCode readValue = mapper.readerFor(ErrorCode.class).readValue(s);

        assertTrue(readValue instanceof ErrorCodeDto);
    }

    @Test
    public void wrappedSerializationOfErrorCodeShouldWork() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ErrorCodeModule());
        TestNestingClass nesting = new TestNestingClass();
        nesting.name = "toto";
        nesting.code = new ErrorCodeDto()
                .setGroup("GROUP")
                .setProduct("PRODUCT")
                .setCode("CODE")
                .setHttpStatus(123)
                .setExpectedContextEntries(singletonList("entry"));
        nesting.otherField = singletonList("ahah");

        String nestingAsText = mapper.writeValueAsString(nesting);

        TestNestingClass readValue = mapper.readerFor(TestNestingClass.class).readValue(nestingAsText);

        assertTrue(readValue.code instanceof ErrorCodeDto);
        assertEquals(nesting.code.getProduct(), readValue.code.getProduct());
        assertEquals(nesting.code.getGroup(), readValue.code.getGroup());
        assertEquals(nesting.code.getCode(), readValue.code.getCode());
        assertEquals(nesting.code.getHttpStatus(), readValue.code.getHttpStatus());
        assertEquals(nesting.code.getExpectedContextEntries(), readValue.code.getExpectedContextEntries());
    }

    private static class TestNestingClass {

        public String name;

        public ErrorCode code;

        public Collection<String> otherField;
    }
}
