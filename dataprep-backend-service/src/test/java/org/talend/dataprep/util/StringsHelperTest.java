package org.talend.dataprep.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringsHelperTest {
    @Test
    public void match_should_succeed_in_strict_mode() throws Exception {
        //given
        final boolean strict = true;
        final String reference = "Jimmy";
        final String value = "jimmy";

        //when
        final boolean result = StringsHelper.match(reference, value, strict);

        //then
        assertTrue(result);
    }

    @Test
    public void match_should_fail_in_strict_mode() throws Exception {
        //given
        final boolean strict = true;
        final String reference = "Jimmy";
        final String value = "imm";

        //when
        final boolean result = StringsHelper.match(reference, value, strict);

        //then
        assertFalse(result);
    }

    @Test
    public void match_should_succeed_in_non_strict_mode() throws Exception {
        //given
        final boolean strict = false;
        final String reference = "Jimmy";
        final String value = "Imm";

        //when
        final boolean result = StringsHelper.match(reference, value, strict);

        //then
        assertTrue(result);
    }

    @Test
    public void match_should_fail_in_non_strict_mode() throws Exception {
        //given
        final boolean strict = false;
        final String reference = "Jimmy";
        final String value = "Jammy";

        //when
        final boolean result = StringsHelper.match(reference, value, strict);

        //then
        assertFalse(result);
    }
}