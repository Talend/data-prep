//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.common;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utils class to ease manipulation of parameters of type ParameterType.REGEX.
 */
public class ReplaceOnValueHelper {

    /** The regex mode parameter name. */
    public static final String REGEX_MODE = "regex";

    /** The value of the 'equals' operator. */
    public static final String EQUALS_MODE = "equals";

    /** The value of the 'contains' operator. */
    public static final String CONTAINS_MODE = "contains";

    /** The starts with parameter name. */
    public static final String STARTS_WITH_MODE = "starts_with";

    /** The ends with parameter name. */
    public static final String ENDS_WITH_MODE = "ends_with";

    /** The token. */
    private String token;

    /** The operator. */
    private String operator;

    /** The pattern, used only in regex mode. */
    private Pattern pattern;

    /** Indicates if the match is strict or not. */
    private boolean strict = true;

    /**
     * Default empty constructor.
     */
    public ReplaceOnValueHelper() {
    }

    /**
     * Constructor.
     *
     * @param token the token.
     * @param operator the operator.
     */
    @JsonCreator
    public ReplaceOnValueHelper(@JsonProperty("token") String token, @JsonProperty("operator") String operator) {
        this.token = token;
        this.operator = operator;
    }


    /**
     * Build a ReplaceOnValueHelper out of the given json string.
     *
     * @param jsonString the json string.
     * @return a ReplaceOnValueHelper out of the given json string.
     */
    public ReplaceOnValueHelper build(String jsonString, boolean strict) {
        if (StringUtils.isEmpty(jsonString)) {
            throw new InvalidParameterException(jsonString + " is not a valid json");
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ReplaceOnValueHelper replaceOnValueHelper = mapper.readValue(jsonString, ReplaceOnValueHelper.class);
            replaceOnValueHelper.setStrict(strict);
            if (replaceOnValueHelper.isValid()) {
                return replaceOnValueHelper;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * @return the Token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the Operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @return true if this helper is strict.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Set the strict mode.
     *
     * @param strict the strict mode to set.
     */
    private void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * @return the pattern.
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @return true if this pattern is valid.
     */
    private boolean isValid() {
        // regex validity check
        final Boolean regexMode = this.operator.equals(REGEX_MODE);

        if (regexMode && pattern == null) {
            String actualPattern = strict ? this.token : ".*" + this.token + ".*";
            try {
                pattern = Pattern.compile(actualPattern, Pattern.DOTALL);
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a string matches token & operator.
     *
     * If operator is REGEX we use the strict field. If strict=true value must matches the regex, if strict=false a part
     * of value must matches the regex.
     *
     * @param value the value to check.
     * @return true if value matches the token regarding operator.
     */
    public boolean matches(String value) {
        if (value == null) {
            return false;
        }
        if (StringUtils.isEmpty(this.token) && !this.getOperator().equals(EQUALS_MODE)) {
            return false;
        }

        if (!isValid()) {
            return false;
        }

        boolean matches = false;
        switch (this.getOperator()) {
            case EQUALS_MODE:
                matches = StringUtils.equals( value, this.getToken());
                break;
            case CONTAINS_MODE:
                matches = value.contains(this.getToken());
                break;
            case STARTS_WITH_MODE:
                matches = value.startsWith(this.getToken());
                break;
            case ENDS_WITH_MODE:
                matches = value.endsWith(this.getToken());
                break;
            case REGEX_MODE:
                final Matcher matcher = pattern.matcher(value);
                matches = matcher.matches();
                break;
        }

        return matches;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ReplaceOnValueParameter{" + "token='" + token + '\'' + ", operator='" + operator + '\'' + ", pattern=" + pattern
                + ", strict=" + strict + '}';
    }

}
