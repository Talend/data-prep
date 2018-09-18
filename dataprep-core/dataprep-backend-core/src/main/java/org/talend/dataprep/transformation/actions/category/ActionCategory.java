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

package org.talend.dataprep.transformation.actions.category;

import java.util.Locale;

import org.talend.dataprep.i18n.ActionsBundle;

/**
 * List all actions category.
 */
public enum ActionCategory {
    /**
     * Actions in this category will be displayed on column's header.
     */
    COLUMN_METADATA("column_metadata"), //
    COLUMNS("columns"), //
    MATH("math"), // for math operations (sum, abs, ...))
    NUMBERS("numbers"), // for numbers manipulation, but not operations (compare, format, ...)
    STRINGS("strings"), //
    STRINGS_ADVANCED("strings_advanced"), //
    SPLIT("split"), //
    DATE("dates"), //
    BOOLEAN("boolean"), //
    DATA_CLEANSING("data_cleansing"), //
    FILTERED("filtered"), //
    DATA_BLENDING("data_blending"), //
    DATA_MASKING("data_masking"), //
    PHONE_NUMBER("phones"), //
    CONVERSIONS("conversions"), //
    DEDUPLICATION("deduplication");

    /** The category display name. */
    private final String displayName;

    /**
     * Create an action category with the given display name.
     *
     * @param displayName the action display name.
     */
    ActionCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the action display name.
     */
    public String getDisplayName(Locale locale) {
        return ActionsBundle.categoryName(null, locale, this.displayName);
    }
}
