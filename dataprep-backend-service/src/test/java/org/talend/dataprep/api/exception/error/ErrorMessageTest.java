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

package org.talend.dataprep.api.exception.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.talend.dataprep.exception.error.APIErrorCodes.*;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNSUPPORTED_CONTENT;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.talend.ServiceBaseTest;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.test.SpringLocalizationRule;

/**
 * @TODO Add a support for internationalized messages
 */
public class ErrorMessageTest extends ServiceBaseTest {

    @Rule
    public SpringLocalizationRule rule = new SpringLocalizationRule(Locale.US);

    @Test
    public void shouldReturnRightErrorMessageWhenHttpStatusIsZero() {
        // given
        ErrorCode errorCode = new ErrorCode() {

            @Override
            public String getProduct() {
                return "TDP";
            }

            @Override
            public String getGroup() {
                return "API";
            }

            @Override
            public int getHttpStatus() {
                return 0;
            }

            @Override
            public Collection<String> getExpectedContextEntries() {
                return Collections.emptyList();
            }

            @Override
            public String getCode() {
                return null;
            }
        };
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("Service unavailable", exception.getMessage());
        assertEquals("An error has occurred", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());
    }

    @Test
    public void shouldReturnRightErrorMessageWhenHttpStatusIs500() {
        // given
        ErrorCode errorCode = new ErrorCode() {

            @Override
            public String getProduct() {
                return "TDP";
            }

            @Override
            public String getGroup() {
                return "API";
            }

            @Override
            public int getHttpStatus() {
                return 500;
            }

            @Override
            public Collection<String> getExpectedContextEntries() {
                return Collections.emptyList();
            }

            @Override
            public String getCode() {
                return null;
            }
        };
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("An unexpected error occurred and we could not complete your last operation. You can continue to use Data Preparation", exception.getMessage());
        assertEquals("An error has occurred", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnsupportedContentThrown() {
        // given
        ErrorCode errorCode = UNSUPPORTED_CONTENT;

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("Unable to create dataset, content is not supported. Try with a csv or xls file!", exception.getMessage());
        assertEquals("Unsupported content", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());
    }

    @Test
    public void shouldReturnRightErrorMessageWhenDatasetStillInUseThrown() {
        // given
        ErrorCode errorCode = DATASET_STILL_IN_USE;

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("You cannot delete the dataset, it is being used by preparation(s)", exception.getMessage());
        assertEquals("Deletion forbidden", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenPreparationStepCannotBeDeletedInSingleModeThrown() {
        // given
        ErrorCode errorCode = PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE;

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("This action cannot be deleted because subsequent actions depend on it", exception.getMessage());
        assertEquals("Delete action not authorized", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());
    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnableToCreateDatasetThrown() {
        // given
        ErrorCode errorCode = UNABLE_TO_CREATE_DATASET;

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("An error occurred during import", exception.getMessage());
        assertEquals("Import error", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());
    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnableToCreateOrUpdateDatasetThrown() {
        // given
        ErrorCode errorCode = UNABLE_TO_CREATE_OR_UPDATE_DATASET;

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("An error occurred during update", exception.getMessage());
        assertEquals("Update error", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenDefaultErrorThrown() {
        // given
        ErrorCode errorCode = new ErrorCode() {

            @Override
            public String getProduct() {
                return "TDP";
            }

            @Override
            public String getGroup() {
                return "API";
            }

            @Override
            public int getHttpStatus() {
                return 404;
            }

            @Override
            public Collection<String> getExpectedContextEntries() {
                return Collections.emptyList();
            }

            @Override
            public String getCode() {
                return null;
            }
        };

        // when
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        assertEquals(errorCode, exception.getCode());
        assertEquals("An unexpected error occurred and we could not complete your last operation. You can continue to use Data Preparation", exception.getMessage());
        assertEquals("An error has occurred", exception.getMessageTitle());
        assertFalse(exception.getContext().entries().iterator().hasNext());
    }

}
