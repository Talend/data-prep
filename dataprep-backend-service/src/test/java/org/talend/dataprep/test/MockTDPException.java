// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

/**
 * Exception that mocks TDPException to be used in tests because you cannot instantiate a TDPException out of a string.
 */
public class MockTDPException extends RuntimeException {

    /**
     * @see RuntimeException
     */
    public MockTDPException(String message) {
        super(message);
    }

}
