// ============================================================================
//
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

package org.talend.dataprep.test;

import com.jayway.restassured.response.Response;

/**
 * Exception that mocks TDPException to be used in tests because you cannot instantiate a TDPException out of a string.
 */
public class MockTDPException extends RuntimeException {

    private int statusCode;

    /**
     * Create a MockTDPException out of the given response.
     *
     * @param response the reponse to create the excepion from.
     */
    public MockTDPException(Response response) {
        super(response.asString());
        this.statusCode = response.getStatusCode();
    }

    /**
     * @return the StatusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
}
