package org.talend.dataprep.test;

import java.util.Locale;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} to set a given {@link Locale locale} before each test execution and rollback to the previous locale
 * once test is over.
 *
 * @see Locale#setDefault(Locale)
 */
public class LocalizationRule implements TestRule {

    protected final Locale locale;

    public LocalizationRule(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final Locale previousLocale = Locale.getDefault();
                try {
                    Locale.setDefault(locale);
                    base.evaluate();
                } finally {
                    Locale.setDefault(previousLocale);
                }
            }
        };
    }
}
