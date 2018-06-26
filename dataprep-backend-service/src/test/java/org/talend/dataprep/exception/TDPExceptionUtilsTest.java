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

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;

import java.io.InputStream;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNABLE_TO_CONNECT_TO_STREAMS;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_SERVICE_EXCEPTION;

/**
 * Unit test for TDPExceptionUtils.
 *
 * @see TDPExceptionUtils
 */
public class TDPExceptionUtilsTest {

    @Test
    public void shouldExtractTDPException() {
        // given
        TDPException expected = new TDPException(UNABLE_TO_CONNECT_TO_STREAMS);
        final HystrixRuntimeException hre = new HystrixRuntimeException( //
                BAD_REQUEST_EXCEPTION, //
                TestHystrixCommand.class, //
                "hello", //
                new Exception("middle", expected), //
                new Throwable("fallback")//
        );

        // when
        final TDPException tdpException = TDPExceptionUtils.processHystrixException(hre);

        // then
        assertNotNull(tdpException);
        assertEquals(UNABLE_TO_CONNECT_TO_STREAMS, tdpException.getCode());
    }

    @Test
    public void shouldFallbackTDPException() {
        // given
        final HystrixRuntimeException hre = new HystrixRuntimeException( //
                BAD_REQUEST_EXCEPTION, //
                TestHystrixCommand.class, //
                "hello", //
                new Exception("middle", new IllegalArgumentException("root")), //
                new Throwable("fallback")//
        );

        // when
        final TDPException tdpException = TDPExceptionUtils.processHystrixException(hre);

        // then
        assertNotNull(tdpException);
        assertEquals(UNEXPECTED_SERVICE_EXCEPTION, tdpException.getCode());
    }

    private static class TestHystrixCommand extends HystrixCommand<InputStream> {

        protected TestHystrixCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("test-group"));
        }

        @Override
        protected InputStream run() throws Exception {
            return null;
        }
    }
}
