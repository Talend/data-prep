package org.talend.dataprep.test;

import java.util.Locale;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * A specialization of {@link LocalizationRule} that takes care of the Spring's {@link LocaleContextHolder} as well.
 *
 * @see LocalizationRule
 */
public class SpringLocalizationRule extends LocalizationRule {

    public SpringLocalizationRule(Locale locale) {
        super(locale);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Statement statement = super.apply(base, description);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final Locale previousLocale = LocaleContextHolder.getLocale();
                try {
                    LocaleContextHolder.setLocale(locale);
                    statement.evaluate();
                } finally {
                    LocaleContextHolder.setLocale(previousLocale);
                }
            }
        };
    }
}
