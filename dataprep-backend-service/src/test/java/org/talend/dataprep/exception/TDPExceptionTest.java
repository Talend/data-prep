// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.junit.Rule;
import org.junit.Test;
import org.talend.dataprep.test.SpringLocalizationRule;

public class TDPExceptionTest {

    @Rule
    public SpringLocalizationRule rule = new SpringLocalizationRule(Locale.FRENCH);

    @Test
    public void getMessage() throws Exception {
        // Given
        Locale.setDefault(Locale.US);

        // When
        final TDPException tdpException = new TDPException(UNEXPECTED_EXCEPTION);

        // Then
        assertThat(tdpException.getMessage(), startsWith("An unexpected error occurred and we could"));
    }

    @Test
    public void getLocalizedMessage() throws Exception {
        // When
        final TDPException tdpException = new TDPException(UNEXPECTED_EXCEPTION);

        // Then
        assertThat(tdpException.getLocalizedMessage(), startsWith("Une erreur inattendue est survenue"));
    }

    @Test
    public void getMessageTitle() throws Exception {
        // When
        final TDPException tdpException = new TDPException(UNEXPECTED_EXCEPTION);

        // Then
        assertThat(tdpException.getMessageTitle(), is("Une erreur est survenue"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void writeTo() throws Exception {
        final TDPException tdpException = new TDPException(UNEXPECTED_EXCEPTION);
        tdpException.writeTo(new StringWriter());
    }

}
