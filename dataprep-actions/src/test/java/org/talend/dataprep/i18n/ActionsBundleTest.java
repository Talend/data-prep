package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.i18n.custom.actions.TestAction;

public class ActionsBundleTest {

    @Test
    public void actionLabel() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(this, Locale.US, "negate"));
    }

    @Test
    public void actionLabel_defaultToEnglish() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(this, Locale.FRANCE, "negate"));
    }

    @Test(expected = TalendRuntimeException.class)
    public void actionLabel_nonexistentThrowsException() throws Exception {
        assertEquals("toto", ActionsBundle.INSTANCE.actionLabel(this, Locale.US, "toto"));
    }

    @Test
    public void actionDescription() throws Exception {
        assertEquals("Reverse the boolean value of cells from this column", ActionsBundle.INSTANCE.actionDescription(this, Locale.US, "negate"));
    }

    @Test
    public void parameterLabel() throws Exception {
        assertEquals("Dataset name", ActionsBundle.INSTANCE.parameterLabel(this, Locale.US, "name"));
    }

    @Test
    public void parameterDescription() throws Exception {
        assertEquals("Name", ActionsBundle.INSTANCE.parameterDescription(this, Locale.US, "name"));
    }

    @Test
    public void choice() throws Exception {
        assertEquals("other", ActionsBundle.INSTANCE.choice(this, Locale.US, "custom"));
    }

    @Test
    public void customActionBundleCache() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.US, "custom"));
        // Test cache of resource bundle
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionLabel() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionDescription() throws Exception {
        assertEquals("Nice custom description", ActionsBundle.INSTANCE.actionDescription(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionParameter() throws Exception {
        final TestAction action = new TestAction();
        assertEquals("Nice custom parameter label", action.getParameters().get(0).getLabel());
        assertEquals("Nice custom parameter description", action.getParameters().get(0).getDescription());
    }

    @Test
    public void customActionMessageDefaultFallback() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(new Object(), Locale.US, "negate"));
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(null, Locale.US, "negate"));
    }

}
