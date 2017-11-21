// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.exception;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.StringWriter;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

public class TDPExceptionTest {

    private TDPException tdpException = new TDPException(UNEXPECTED_EXCEPTION);

    @Before
    public void setUpLocale() {
        Locale.setDefault(Locale.US);
        LocaleContextHolder.setLocale(Locale.FRANCE);
    }

    @Test
    public void getMessage() throws Exception {
        assertThat(tdpException.getMessage(), startsWith("Sorry an unexpected error occurred and we could"));
    }

    @Test
    public void getLocalizedMessage() throws Exception {
        assertThat(tdpException.getLocalizedMessage(), startsWith("Une erreur inattendue est survenue"));
    }

    @Test
    public void getMessageTitle() throws Exception {
        assertThat(tdpException.getMessageTitle(), is("Une erreur est survenue"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void writeTo() throws Exception {
        tdpException.writeTo(new StringWriter());
    }

}
