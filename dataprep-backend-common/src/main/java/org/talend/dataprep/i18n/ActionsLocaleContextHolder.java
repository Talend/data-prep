/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.i18n;

import static java.util.Locale.US;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Dirty copy of spring LocaleContextHolder.
 */
public abstract class ActionsLocaleContextHolder {

    private static Supplier<Locale> localeSupplier = () -> US;

    public static void setLocaleProvider(Supplier<Locale> provider) {
        ActionsLocaleContextHolder.localeSupplier = provider;
    }

    public static Locale getLocale() {
        Locale locale = localeSupplier.get();
        return (locale != null ? locale : Locale.getDefault());
    }

}
