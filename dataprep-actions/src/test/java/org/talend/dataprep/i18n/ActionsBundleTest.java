package org.talend.dataprep.i18n;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionsBundleTest {

    @Test
    public void actionLabel() throws Exception {
        assertEquals("Negate Value", ActionsBundle.actionLabel(Locale.US, "negate"));
    }

    @Test
    public void actionLabel_defaultToEnglish() throws Exception {
        assertEquals("Negate Value", ActionsBundle.actionLabel(Locale.FRANCE, "negate"));
    }

    @Test(expected = MissingResourceException.class)
    public void actionLabel_nonexistentThrowsException() throws Exception {
        assertEquals("Negate Value", ActionsBundle.actionLabel(Locale.US, "toto"));
    }

    @Test
    public void actionDescription() throws Exception {
        assertEquals("Reverse the boolean value of cells from this column", ActionsBundle.actionDescription(Locale.US, "negate"));
    }

    @Test
    public void parameterLabel() throws Exception {
        assertEquals("Dataset name", ActionsBundle.parameterLabel(Locale.US, "name"));
    }

    @Test
    public void parameterDescription() throws Exception {
        assertEquals("Name", ActionsBundle.parameterDescription(Locale.US, "name"));
    }

    @Test
    public void choice() throws Exception {
        assertEquals("other", ActionsBundle.choice(Locale.US, "custom"));
    }

}
