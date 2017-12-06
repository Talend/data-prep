package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.i18n.custom.actions.TestAction;
import org.talend.dataprep.test.LocalizationRule;
import org.talend.dataprep.transformation.actions.bool.Negate;

public class ActionsBundleTest {

    @Rule
    public LocalizationRule rule = new LocalizationRule(Locale.US);

    @Test
    public void actionLabel() throws Exception {
        assertEquals("Negate value", ActionsBundle.actionLabel(this, Locale.US, "negate"));
    }

    @Test
    public void actionLabel_french() throws Exception {
        assertEquals("Calculer l'opposé", ActionsBundle.actionLabel(this, Locale.FRENCH, "negate"));
    }

    @Test
    public void actionLabel_defaultToEnglish() throws Exception {
        assertEquals("Negate value", ActionsBundle.actionLabel(this, Locale.KOREAN, "negate"));
    }

    @Test(expected = TalendRuntimeException.class)
    public void actionLabel_nonexistentThrowsException() throws Exception {
        assertEquals("toto", ActionsBundle.actionLabel(this, Locale.US, "toto"));
    }

    @Test
    public void actionDescription() throws Exception {
        assertEquals("Reverse the boolean value of cells from this column",
                ActionsBundle.actionDescription(this, Locale.US, "negate"));
    }

    @Test
    public void emptyActionDocUrl() throws Exception {
        assertEquals("", ActionsBundle.actionDocUrl(this, Locale.US, "uppercase"));
    }

    @Test
    public void parameterLabel() throws Exception {
        assertEquals("Dataset name", ActionsBundle.parameterLabel(this, Locale.US, "name"));
    }

    @Test
    public void parameterDescription() throws Exception {
        assertEquals("Name", ActionsBundle.parameterDescription(this, Locale.US, "name"));
    }

    @Test
    public void choice() throws Exception {
        assertEquals("Other", ActionsBundle.choice(this, Locale.US, "custom"));
    }

    @Test
    public void customActionBundleCache() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.actionLabel(new TestAction(), Locale.US, "custom"));
        // Test cache of resource bundle
        assertEquals("Nice custom label", ActionsBundle.actionLabel(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionLabel() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.actionLabel(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionLabel_fallbacks() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.actionLabel(new Negate(), Locale.US, "custom"));
    }

    @Test
    public void customActionDescription() throws Exception {
        assertEquals("Nice custom description",
                ActionsBundle.actionDescription(new TestAction(), Locale.US, "custom"));
    }

    @Test
    public void customActionParameter() throws Exception {
        final TestAction action = new TestAction();
        assertEquals("Nice custom parameter label", action.getParameters(Locale.US).get(0).getLabel());
        assertEquals("Nice custom parameter description", action.getParameters(Locale.US).get(0).getDescription());
    }

    @Test
    public void customActionMessageDefaultFallback() throws Exception {
        assertEquals("Negate value", ActionsBundle.actionLabel(new Object(), Locale.US, "negate"));
        assertEquals("Negate value", ActionsBundle.actionLabel(null, Locale.US, "negate"));
    }

}
