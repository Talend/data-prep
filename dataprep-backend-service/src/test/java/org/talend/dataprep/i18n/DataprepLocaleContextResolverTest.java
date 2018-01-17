/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;

@RunWith(MockitoJUnitRunner.class)
public class DataprepLocaleContextResolverTest {

    private static final String TEST_LOCALE = "vi-VN";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    public void resolveLocaleContext_wellFormattedLocaleIsTaken() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver(TEST_LOCALE);

        LocaleContext localeContext = resolver.resolveLocaleContext(request);
        assertNotNull(localeContext);
        assertEquals(TEST_LOCALE, localeContext.getLocale().toLanguageTag());
    }

    @Test
    public void resolveLocaleContext_notFormattedLocaleDefault() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver("abcd");

        LocaleContext localeContext = resolver.resolveLocaleContext(request);
        assertNotNull(localeContext);
        assertEquals(Locale.US, localeContext.getLocale());
    }

    @Test
    public void resolveLocaleContext_noLocaleThenDefault() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver(null);

        LocaleContext localeContext = resolver.resolveLocaleContext(request);
        assertNotNull(localeContext);
        assertEquals(Locale.US, localeContext.getLocale());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setLocaleContext() throws Exception {
        new DataprepLocaleContextResolver("en-US").setLocaleContext(request, response, new SimpleLocaleContext(Locale.FRANCE));
    }

}
