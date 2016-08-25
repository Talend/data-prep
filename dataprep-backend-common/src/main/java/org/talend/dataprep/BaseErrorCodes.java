package org.talend.dataprep;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

public enum BaseErrorCodes implements ErrorCode {

    UNABLE_TO_PARSE_JSON(500),
    UNEXPECTED_EXCEPTION(500),
    MISSING_ACTION_SCOPE(400),
    UNSUPPORTED_ACTION_SCOPE(500),
    MISSING_ACTION_SCOPE_PARAMETER(500),
    UNABLE_TO_PARSE_FILTER(400),
    MISSING_I18N(500);

    /** The http status to use. */
    private final int httpStatus;

    /** Expected entries to be in the context. */
    private final List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    BaseErrorCodes(int httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    @Override
    public String getProduct() {
        return "TDP";
    }

    @Override
    public String getGroup() {
        return "BASE";
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public Collection<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    @Override
    public String getCode() {
        return this.toString();
    }
}
