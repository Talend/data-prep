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

package org.talend.dataprep.exception;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.exception.error.ErrorMessage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Class for all business (TDP) exception.
 */
public class TDPException extends TalendRuntimeException {

    private static final long serialVersionUID = -51732176302413600L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TDPException.class);

    /**
     * this field if set to <code>true</code> will prevent {@link TDPExceptionController} to log a stack trace
     */
    private boolean error = false;

    private final String message;

    private final String messageTitle;

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
        // Translation done at the object creation
        List<Object> values;
        values = context == null ? emptyList() //
                : stream(context.entries().spliterator(), false).map(Map.Entry::getValue).collect(toList());
        message = ErrorMessage.getMessage(getCode(), values.toArray(new Object[values.size()]));
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
     * Lightweight constructor without a cause.
     *
     * @param code the error code that holds all the .
     * @param context the exception context.
     */
    public TDPException(ErrorCode code, ExceptionContext context, boolean error) {
        this(code, null, context);
        this.error = error;
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

    /**
     * @return <code>true</code> if exception is used to convey an error. In this case, stack trace is less important.
     */
    public boolean isError() {
        return error;
    }

    @Override
    public void writeTo(Writer writer) {

        try {
            JsonGenerator generator = (new JsonFactory()).createGenerator(writer);
            generator.writeStartObject();
            writeErrorContent(generator);
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            LOGGER.error("Unable to write exception to " + writer + ".", e);
        }

    }

    private void writeErrorContent(JsonGenerator generator) throws IOException {
        generator.writeStringField("code", getCode().getProduct() + '_' + getCode().getGroup() + '_' + getCode().getCode());
        generator.writeStringField("message", message);
        generator.writeStringField("message_title", messageTitle);
        if (getCause() != null) {
            generator.writeStringField("cause", getCause().getMessage());
        }
        if (getContext() != null) {
            generator.writeFieldName("context");
            generator.writeStartObject();
            for (Map.Entry<String, Object> entry : getContext().entries()) {
                generator.writeStringField(entry.getKey(), entry.getValue().toString());
            }
            generator.writeEndObject();
        }
    }

}
