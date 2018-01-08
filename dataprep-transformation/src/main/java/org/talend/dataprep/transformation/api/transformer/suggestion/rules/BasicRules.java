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

package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Implements a set of basic rules (see specifications in <a href="https://jira.talendforge.org/browse/TDP-393">Jira</a>
 * ).
 */
public class BasicRules {

    /**
     * A helper to filter {@link Type#NUMERIC} columns.
     */
    protected static final Predicate<ColumnMetadata> IS_NUMERIC = columnMetadata -> {
        final Type type = Type.get(columnMetadata.getType());
        return Type.NUMERIC.isAssignableFrom(type);
    };

    /**
     * A helper to filter {@link Type#STRING} columns.
     */
    protected static final Predicate<ColumnMetadata> IS_STRING = columnMetadata -> {
        final Type type = Type.get(columnMetadata.getType());
        return Type.STRING.equals(type);
    };

    /**
     * A helper to filter {@link Type#DATE} columns (including column where semantic domain is 'date').
     */
    protected static final Predicate<ColumnMetadata> IS_DATE = columnMetadata -> {
        final Type type = Type.get(columnMetadata.getType());
        return Type.DATE.isAssignableFrom(type) || "date".equalsIgnoreCase(columnMetadata.getDomain());
    };

    /**
     * A helper to filter columns where semantic domain=email.
     */
    protected static final Predicate<ColumnMetadata> IS_EMAIL = columnMetadata -> SemanticCategoryEnum.EMAIL.getId()
            .equalsIgnoreCase(columnMetadata.getDomain());

    /**
     * A helper to filter columns where semantic domain=url.
     */
    protected static final Predicate<ColumnMetadata> IS_URL = columnMetadata -> SemanticCategoryEnum.URL.getId()
            .equalsIgnoreCase(columnMetadata.getDomain());

    /**
     * A helper to filter columns where semantic domain=phone.
     */
    protected static final Predicate<ColumnMetadata> IS_PHONE = columnMetadata -> columnMetadata.getDomain().toLowerCase().endsWith("phone");

}
