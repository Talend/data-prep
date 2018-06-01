package org.talend.dataprep.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.slf4j.Logger;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.beans.PropertyEditorSupport;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

public class ConverterBasedPropertyEditor<T> extends PropertyEditorSupport {

    private static final Logger LOGGER = getLogger(ConverterBasedPropertyEditor.class);

    private static final Converter<String, String> camelToSnakeCaseConverter = CaseFormat.LOWER_CAMEL
            .converterTo(CaseFormat.UPPER_UNDERSCORE);

    private final Function<String, T> converter;

    public ConverterBasedPropertyEditor(Function<String, T> converter) {
        this.converter = converter;
    }

    @Override
    public void setAsText(String text) {
        String fromCamelCase = camelToSnakeCaseConverter.convert(text);
        T value;
        try {
            value = converter.apply(fromCamelCase);
        } catch (IllegalArgumentException e) {
            LOGGER.trace("Could not read parameter as camel case.", e);
            try {
                value = converter.apply(text.toUpperCase());
            } catch (IllegalArgumentException e2) {
                LOGGER.trace("Could not read parameter as snake case.", e2);
                throw new TDPException(CommonErrorCodes.ILLEGAL_SORT_FOR_LIST, e2);
            }
        }
        setValue(value);
    }
}
