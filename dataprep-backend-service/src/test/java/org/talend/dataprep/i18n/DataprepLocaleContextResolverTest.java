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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataprepLocaleContextResolverTest {

    private static final String TEST_LOCALE = "vi-VN";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    public void resolveLocaleContextWithWellFormattedLocaleIsTaken() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver(TEST_LOCALE);

        Locale locale = resolver.resolveLocale(request);
        assertNotNull(locale);
        assertEquals(TEST_LOCALE, locale.toLanguageTag());
    }

    @Test
    public void resolveLocaleContextWithNotFormattedLocaleDefault() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver("abcd");

        Locale locale = resolver.resolveLocale(request);
        assertNotNull(locale);
        assertEquals(Locale.US, locale);
    }

    @Test
    public void resolveLocaleContextWithNoLocaleThenDefault() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver(null);

        Locale locale = resolver.resolveLocale(request);
        assertNotNull(locale);
        assertEquals(Locale.US, locale);
    }

    @Test
    public void resolveLocaleContextWithFromHttpHeader() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver(null);
        when(request.getHeader(eq(HttpHeaders.ACCEPT_LANGUAGE))).thenReturn(Locale.JAPANESE.toLanguageTag());
        when(request.getLocales()).thenReturn(Collections.enumeration(Arrays.asList(Locale.JAPANESE, Locale.US)));

        Locale locale = resolver.resolveLocale(request);
        assertNotNull(locale);
        assertEquals(Locale.JAPANESE, locale);
    }

    @Test
    public void resolveLocaleContextWithMalformedLocale() throws Exception {
        DataprepLocaleContextResolver resolver = new DataprepLocaleContextResolver("@@&&");

        Locale locale = resolver.resolveLocale(request);
        assertNotNull(locale);
        assertEquals(Locale.US, locale);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setLocaleContext() throws Exception {
        new DataprepLocaleContextResolver("en-US").setLocale(request, response, Locale.FRANCE);
    }

}
