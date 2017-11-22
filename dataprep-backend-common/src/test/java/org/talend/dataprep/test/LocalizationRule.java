package org.talend.dataprep.test;

import java.util.Locale;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class LocalizationRule implements TestRule {

    private final Locale locale;

    public LocalizationRule(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Locale previousLocale = Locale.getDefault();
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
