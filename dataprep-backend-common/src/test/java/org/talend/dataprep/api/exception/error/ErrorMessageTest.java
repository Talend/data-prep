//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.exception.error;

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.exception.error.APIErrorCodes.*;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNSUPPORTED_CONTENT;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.exception.TDPException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ErrorMessageTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
/**
 * @TODO Add a support for internationalized messages
 */
public class ErrorMessageTest {

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
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "Service unavailable" + "\",\"message_title\":Server error,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

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
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "An error occurred" + "\",\"message_title\":Server error,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnsupportedContentThrown() {
        // given
        ErrorCode errorCode = UNSUPPORTED_CONTENT;
        TDPException unsupportedContent = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "Unable to create data set, content is not supported. Try with a csv or xls file!"
                + "\",\"message_title\":Unsupported content,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        unsupportedContent.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenDatasetStillInUseThrown() {
        // given
        ErrorCode errorCode = DATASET_STILL_IN_USE;
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "Deletion not allowed, dataset still used by preparation(s)"
                + "\",\"message_title\":Deletion forbidden,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenPreparationStepCannotBeDeletedInSingleModeThrown() {
        // given
        ErrorCode errorCode = PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE;
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "This action cannot be deleted alone because other following actions depends on it"
                + "\",\"message_title\":Delete action not authorized,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnableToCreateDatasetThrown() {
        // given
        ErrorCode errorCode = UNABLE_TO_CREATE_DATASET;
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "An error occurred during import" + "\",\"message_title\":Import error,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

    }

    @Test
    public void shouldReturnRightErrorMessageWhenUnableToCreateOrUpdateDatasetThrown() {
        // given
        ErrorCode errorCode = UNABLE_TO_CREATE_OR_UPDATE_DATASET;
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "An error occurred during update" + "\",\"message_title\":Update error,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

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
        TDPException exception = new TDPException(errorCode, null, null);

        // then
        String errorExpected = "{\"code\":\"" + errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode()
                + "\",\"message\":\"" + "An error occurred" + "\",\"message_title\":Server error,\"context\":{}}";
        StringWriter stringWriter = new StringWriter();
        exception.writeTo(stringWriter);

        assertThat(stringWriter.toString(), sameJSONAs(errorExpected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());
    }

}
