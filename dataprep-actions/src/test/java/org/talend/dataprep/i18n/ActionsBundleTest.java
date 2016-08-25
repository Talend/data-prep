package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;

public class ActionsBundleTest {

    @Test
    public void actionLabel() throws Exception {
        assertEquals("Negate Value", ActionsBundle.INSTANCE.actionLabel(Locale.US, "negate"));
    }

    @Test
    public void actionLabel_defaultToEnglish() throws Exception {
        assertEquals("Negate Value", ActionsBundle.INSTANCE.actionLabel(Locale.FRANCE, "negate"));
    }

    @Test(expected = TalendRuntimeException.class)
    public void actionLabel_nonexistentThrowsException() throws Exception {
        assertEquals("toto", ActionsBundle.INSTANCE.actionLabel(Locale.US, "toto"));
    }

    @Test
    public void actionDescription() throws Exception {
        assertEquals("Reverse the boolean value of cells from this column", ActionsBundle.INSTANCE.actionDescription(Locale.US, "negate"));
    }

    @Test
    public void parameterLabel() throws Exception {
        assertEquals("Dataset name", ActionsBundle.INSTANCE.parameterLabel(Locale.US, "name"));
    }

    @Test
    public void parameterDescription() throws Exception {
        assertEquals("Name", ActionsBundle.INSTANCE.parameterDescription(Locale.US, "name"));
    }

    @Test
    public void choice() throws Exception {
        assertEquals("other", ActionsBundle.INSTANCE.choice(Locale.US, "custom"));
    }

}
