/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.preparation.service;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.INVALID_PREPARATION;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;

public class PreparationValidator {

    /**
     * Runs validation on this preparation and if any violation is found, it throws an exception else it returns.
     *
     * @param preparation the preparation to validate
     */
    public static void validateOrthrowException(Preparation preparation) {
        Set<Violation> validation = validate(preparation);
        if (!validation.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (Violation violation : validation) {
                message.append(violation.getMessage());
                message.append("\n");
            }
            throw new TDPException(INVALID_PREPARATION, ExceptionContext.build().put("message", message));
        }
    }

    @NotNull
    public static Set<Violation> validate(Preparation preparation) {
        Set<Violation> validation;
        if (preparation == null) {
            validation = singleton(new Violation("Preparation is null", null));
        } else {
            if (preparation.getRowMetadata() == null) {
                validation = singleton(new Violation("Preparation row metadata is null", "rowMetadata"));
            } else {
                validation = emptySet();
            }
        }
        return validation;
    }

    public static class Violation {

        private final String message;

        private final String field;

        public Violation(String message, String field) {
            this.message = message;
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public String getField() {
            return field;
        }
    }
}
