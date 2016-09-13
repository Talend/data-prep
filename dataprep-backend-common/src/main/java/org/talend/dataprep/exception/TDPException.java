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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.exception.error.ErrorMessage;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Full constructor.
     *
     * @param code the error code that holds all the .
     * @param cause the root cause of this error.
     * @param context the context of the error when it occurred (used to detail the user error message in frontend).
     */
    public TDPException(ErrorCode code, Throwable cause, ExceptionContext context) {
        super(code, cause, context);
    }

    /**
     * Lightweight constructor without context.
     *
     * @param code the error code that holds all the .
     * @param cause the root cause of this error.
     */
    public TDPException(ErrorCode code, Throwable cause) {
        super(code, cause, null);
    }

    /**
     * Lightweight constructor without a cause.
     *
     * @param code the error code that holds all the .
     * @param context the exception context.
     */
    public TDPException(ErrorCode code, ExceptionContext context) {
        super(code, null, context);
    }

    /**
     * Lightweight constructor without a cause.
     *
     * @param code the error code that holds all the .
     * @param context the exception context.
     */
    public TDPException(ErrorCode code, ExceptionContext context, boolean error) {
        super(code, null, context);
        this.error = error;
    }    
    
    /**
     * Basic constructor from a JSON error code.
     *
     * @param code an error code serialized to JSON.
     */
    public TDPException(JsonErrorCode code) {
        super(code, ExceptionContext.build().from(code.getContext()));
    }

    /**
     * Basic constructor with the bare error code.
     *
     * @param code the error code that holds all the .
     */
    public TDPException(ErrorCode code) {
        super(code, null, null);
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
            {
                generator.writeStringField("code",
                        getCode().getProduct() + '_' + getCode().getGroup() + '_' + getCode().getCode());
                List<String> values = getContextValues();
                String message = ErrorMessage.getMessage(getCode(), values.toArray(new String[values.size()]));
                String messageTitle = ErrorMessage.getMessageTitle(getCode(), values.toArray(new String[values.size()]));
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
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            LOGGER.error("Unable to write exception to " + writer + ".", e);
        }

    }

    /**
     * Method needed as long as the context does not expose a values() method.
     * @return the context values.
     */
    private List<String> getContextValues() {
        List<String> values = new ArrayList<>();
        getContext().entries().forEach(e -> values.add(e.getValue().toString()));
        return values;
    }
}
