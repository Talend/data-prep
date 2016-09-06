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

package org.talend.dataprep.api.service.api.exception.error;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.Application;
import org.talend.dataprep.exception.error.*;
import org.talend.dataprep.i18n.MessagesBundle;

/**
 * Test that each error code has a message and a message title in the (default language) error message properties file.
 * see org.talend.dataprep/error_messages.properties
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@WebAppConfiguration
public class ErrorCodeMessagesTest {

    @Autowired
    MessagesBundle messagesBundle;

    /**
     * Asserts that the specified errorCode exits in the error_message properties.
     * 
     * @param code
     */
    private void assertErrorCodeMessageExists(final String code) {
        String suffixedCode = code + ErrorMessage.MESSAGE_SUFFIX;
        String message = messagesBundle.getString(suffixedCode);
        Assert.assertFalse("The following error code: "+code+ " is not associated with a body message",StringUtils.isEmpty(message) || suffixedCode.equals(message));
    }

    private void assertErrorCodeMessageTitleExists(final String code) {
        String suffixedCode = code + ErrorMessage.TITLE_SUFFIX;
        String title = messagesBundle.getString(suffixedCode);
        Assert.assertFalse("The following error code: "+code+ " is not associated with a title message", StringUtils.isEmpty(title) || suffixedCode.equals(title));
    }

    @Test
    public void testCommonErrorCodeMessages() {
        for (CommonErrorCodes code : CommonErrorCodes.values()) {
            assertErrorCodeMessageExists(code.getCode());
            assertErrorCodeMessageTitleExists(code.getCode());
        }
    }

    @Test
    public void testAPIErrorCodeMessages() {
        for (APIErrorCodes code : APIErrorCodes.values()) {
            assertErrorCodeMessageExists(code.getCode());
            assertErrorCodeMessageTitleExists(code.getCode());
        }
    }

    @Test
    public void testDatasetErrorCodeMessages() {
        for (DataSetErrorCodes code : DataSetErrorCodes.values()) {
            assertErrorCodeMessageExists(code.getCode());
            assertErrorCodeMessageTitleExists(code.getCode());
        }
    }

    @Test
    public void testTransformationErrorCodeMessages() {
        for (TransformationErrorCodes code : TransformationErrorCodes.values()) {
            assertErrorCodeMessageExists(code.getCode());
            assertErrorCodeMessageTitleExists(code.getCode());
        }
    }

    @Test
    public void testPreparationErrorCodeMessages() {
        for (PreparationErrorCodes code : PreparationErrorCodes.values()) {
            assertErrorCodeMessageExists(code.getCode());
            assertErrorCodeMessageTitleExists(code.getCode());
        }
    }

}
