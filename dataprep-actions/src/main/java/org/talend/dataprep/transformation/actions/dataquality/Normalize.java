// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.dataquality;

import static java.util.Collections.singletonList;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Lower case a column in a dataset row.
 */
@Action(Normalize.ACTION_NAME)
public class Normalize extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "normalize"; //$NON-NLS-1$

    protected static final String NEW_COLUMN_SUFFIX = "_normalized";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return ActionsUtils.appendColumnCreationParameter(super.getParameters(locale), locale, CREATE_NEW_COLUMN_DEFAULT);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context,
                    singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX)));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (value != null) {
            row.set(ActionsUtils.getTargetColumnId(context), normalize(value));
        }
    }

    /**
     * Return the normalized given string.
     *
     * @param string the string to normalized.
     * @return the normalized given string.
     */
    protected String normalize(final String string) {
        // Convert input string to decomposed Unicode (NFD) so that the
        // diacritical marks used in many European scripts (such as the
        // "C WITH CIRCUMFLEX" → ĉ) become separate characters.
        // Also use compatibility decomposition (K) so that characters,
        // that have the exact same meaning as one or more other
        // characters (such as "㎏" → "kg" or "ﾋ" → "ヒ"), match when
        // comparing them.
        String normalized = Normalizer.normalize(string, Normalizer.Form.NFKD);

        StringBuilder result = new StringBuilder();

        int offset = 0, strLen = normalized.length();
        while (offset < strLen) {
            int character = normalized.codePointAt(offset);
            offset += Character.charCount(character);

            // Only process characters that are not combining Unicode
            // characters. This way all the decomposed diacritical marks
            // (and some other not-that-important modifiers), that were
            // part of the original string or produced by the NFKD
            // normalizer above, disappear.
            switch (Character.getType(character)) {
            case Character.NON_SPACING_MARK:
            case Character.COMBINING_SPACING_MARK:
                // Some combining character found
                break;

            default:
                result.appendCodePoint(Character.toLowerCase(character));
            }
        }

        // Since we stripped all combining Unicode characters in the
        // previous while-loop there should be no combining character
        // remaining in the string and the composed and decomposed
        // versions of the string should be equivalent. This also means
        // we do not need to convert the string back to composed Unicode
        // before returning it.
        return result.toString().trim();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
