package org.talend.dataprep.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

    @Test
    /**
     * Normalized a string to
     */
    public void normalizeString() throws Exception {
        // Unicode: 'v' 118, 'i' 105, 's' 115, 'i' 105, 't' 116, 'e' 101, '́' 769, 's' 115
        final String initialString = "visités";
        // Unicode: 'v' 118, 'i' 105, 's' 115, 'i' 105, 't' 116, 'é' 233, 's' 115
        final String expected = "visités";

        assertNotEquals(initialString, expected);

        assertEquals(expected, StringsHelper.normalizeString(initialString));
        assertEquals(expected, StringsHelper.normalizeString(expected));
        assertEquals(expected, StringsHelper.normalizeString(StringsHelper.normalizeString(initialString)));
    }
}
