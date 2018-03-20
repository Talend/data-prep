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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.exception.error.ErrorMessage;

import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * Class for all business (TDP) exception.
 */
public class TDPException extends TalendRuntimeException {

    private static final long serialVersionUID = -51732176302413600L;

    /**
     * If the exception is a TDPException, rethrow it, else wrap it and then throw it. ClassPostProcessor : Cannot
     * enhance @Configuration bean definition 'refreshScope' since its singleton instance has been created too early. The
     * typical cause is a non-static @Bean method with a BeanDefinitionRegistryPostProcessor return type: Consider declaring
     * such methods as 'static'.
     *
     * @param throwable
     */
    public static RuntimeException rethrowOrWrap(Throwable throwable) {
        return rethrowOrWrap(throwable, UNEXPECTED_EXCEPTION);
    }

    /**
     * If the exception is a TDPException, rethrow it, else wrap it and then throw it.
     *
     * @param throwable The throwable to rethrow or wrap in a TDPException.
     * @param errorCode The Error code to use when you wrap the throwable.
     */
    public static RuntimeException rethrowOrWrap(Throwable throwable, ErrorCode errorCode) {
        if (throwable instanceof TDPException) {
            throw (TDPException) throwable;
        } else if (throwable instanceof HystrixRuntimeException) {
            throw TDPExceptionUtils.processHystrixException((HystrixRuntimeException) throwable);
        } else if (throwable.getCause() instanceof TDPException) {
            throw (TDPException) (throwable.getCause());
        } else {
            throw new TDPException(errorCode, throwable);
        }
    }

    private String message;

    private String localizedMessage;

    private String messageTitle;

    /** Build a blank TDP unexpected exception. **/
    // Needed to be able to convert with conversionService
    public TDPException() {
        super(UNEXPECTED_EXCEPTION);
    }

    /**
     * Build a Talend exception with no i18n handling internally. It is useful when the goal is to just pass an exception in a
     * component
     * that does not have access to the exception bundle.
     */
    public TDPException(ErrorCode code, Throwable cause, String message, String messageTitle, ExceptionContext context) {
        super(code, cause, context);
        this.message = message;
        this.messageTitle = messageTitle;

        // Translation done at the object creation
        List<Object> values = getValuesFromContext(context);
        this.localizedMessage = ErrorMessage.getMessage(getCode(), values.toArray(new Object[values.size()]));
    }

    /**
     * Build a Talend exception that can be interpreted throughout the application and handled by the HTTP API to translate into
     * a meaningful internationalized error message to the end-user.
     *
     * @param code the error code that identify uniquely this error and bind to an i18ned message
     * @param cause the root cause if any of this error.
     * @param context the context of the error depending on the {@link ErrorCode}. It allow i18n messages to be built.
     */
    public TDPException(ErrorCode code, Throwable cause, ExceptionContext context) {
        super(code, cause, context);

        List<Object> values = getValuesFromContext(context);
        message = ErrorMessage.getDefaultMessage(getCode(), values.toArray(new Object[values.size()]));
        localizedMessage = ErrorMessage.getMessage(getCode(), values.toArray(new Object[values.size()]));
        messageTitle = ErrorMessage.getMessageTitle(getCode(), values.toArray(new Object[values.size()]));
    }

    /**
     * Lightweight constructor without context.
     *
     * @param code the error code that holds all the .
     * @param cause the root cause of this error.
     */
    public TDPException(ErrorCode code, Throwable cause) {
        this(code, cause, null);
    }

    /**
     * Lightweight constructor without a cause.
     *
     * @param code the error code that holds all the .
     * @param context the exception context.
     */
    public TDPException(ErrorCode code, ExceptionContext context) {
        this(code, null, context);
    }

    /**
     * Basic constructor from a JSON error code.
     *
     * @param code an error code serialized to JSON.
     */
    public TDPException(JsonErrorCode code) {
        this(code, ExceptionContext.build().from(code.getContext()));
    }

    /**
     * Basic constructor with the bare error code.
     *
     * @param code the error code that holds all the .
     */
    public TDPException(ErrorCode code) {
        this(code, null, null);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use {@link TdpExceptionDto} for serialization.
     * @param writer
     */
    @Override
    @Deprecated
    public void writeTo(Writer writer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    // Needed to keep the compatibility with the deprecated writeTo(Writer) method.
    // This code duplicates the one in ExceptionsConfiguration and should not be used anywhere else.
    private static TdpExceptionDto toExceptionDto(TalendRuntimeException internal) {
        ErrorCode errorCode = internal.getCode();
        String serializedCode = errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode();
        String defaultMessage = internal.getMessage();
        String message = internal.getLocalizedMessage();
        String messageTitle = internal instanceof TDPException ? ((TDPException) internal).getMessageTitle() : null;
        TdpExceptionDto cause =
                internal.getCause() instanceof TDPException ? toExceptionDto((TDPException) internal.getCause()) : null;
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<String, Object> contextEntry : internal.getContext().entries()) {
            context.put(contextEntry.getKey(), contextEntry.getValue());
        }
        return new TdpExceptionDto(serializedCode, cause, defaultMessage, message, messageTitle, context);
    }

    /**
     * Return thie list of object store in the context
     *
     * @param context the error context
     *
     * @return the list of object in the context of the error
     */
    private List<Object> getValuesFromContext(ExceptionContext context) {
        List<Object> values = emptyList();
        if (context != null) {
            values = stream(context.entries().spliterator(), false).map(Map.Entry::getValue).collect(toList());
        }
        return values;
    }

}
