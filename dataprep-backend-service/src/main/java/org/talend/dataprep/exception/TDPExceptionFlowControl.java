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

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * This class should disapear with <a href="https://jira.talendforge.org/browse/TDP-6493">TDP-6493</a>.
 * It is used to compensate a misused of Exception pattern in order to not compute and
 * not display stacktrace in the logs
 *
 */
public class TDPExceptionFlowControl extends TDPException {

    // Needed to be able to convert with conversionService
    public TDPExceptionFlowControl() {
    }

    /**
     * Lightweight constructor without a cause.
     *
     * @param code the error code that holds all the .
     * @param context the exception context.
     */
    public TDPExceptionFlowControl(ErrorCode code, ExceptionContext context) {
        super(code, null, context);
    }

    /**
     * Build a Talend exception with no i18n handling internally. It is useful when the goal is to just pass an
     * exception in a
     * component
     * that does not have access to the exception bundle.
     */
    public TDPExceptionFlowControl(ErrorCode code, Throwable cause, String message, String messageTitle,
            ExceptionContext context) {
        super(code, cause, message, messageTitle, context);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
