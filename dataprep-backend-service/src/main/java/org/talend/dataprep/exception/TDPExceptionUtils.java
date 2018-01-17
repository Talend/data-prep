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

package org.talend.dataprep.exception;

import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * Utility class to deal with TDPException.
 */
class TDPExceptionUtils {

    /**
     * Default empty constructor.
     */
    private TDPExceptionUtils() {
        // private constructor to ensure Utility class
    }

    /**
     * Tries to extract the TDPException out of the HystrixRuntimeException. If no TDPException is found, the
     * HystrixRuntimeException is wrapped into a TDPException.
     *
     * @param hre the HystrixRuntimeException to process.
     * @return the TDPException to throw.
     */
    static TDPException processHystrixException(HystrixRuntimeException hre) {
        // filter out hystrix exception level if possible
        Throwable e = hre;
        TDPException innerMostTDPException = null;
        while (e.getCause() != null) {
            e = e.getCause();
            if (e instanceof TDPException) {
                innerMostTDPException = (TDPException) e;
            }
        }
        if (innerMostTDPException != null) {
            return innerMostTDPException;
        } else {
            return new TDPException(CommonErrorCodes.UNEXPECTED_SERVICE_EXCEPTION, hre);
        }
    }
}
