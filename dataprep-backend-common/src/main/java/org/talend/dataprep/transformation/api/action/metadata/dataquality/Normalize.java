package org.talend.dataprep.transformation.api.action.metadata.dataquality;

import java.text.Normalizer;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Lower case a column in a dataset row.
 */
@Component(Normalize.ACTION_BEAN_PREFIX + Normalize.ACTION_NAME)
public class Normalize extends ActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "normalize"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);
        if (value != null) {
            row.set(columnId, normalize(value));
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
}
