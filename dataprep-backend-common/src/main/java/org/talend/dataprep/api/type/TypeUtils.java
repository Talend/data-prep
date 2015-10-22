package org.talend.dataprep.api.type;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.statistics.SemanticType;
import org.talend.datascience.common.inference.type.DataType;

public class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Compute the dataset metadata columns valid/invalid, empty/count values.
     *
     * @return the dataset column types in DQ libraries.
     * @param columns The Data Prep {@link ColumnMetadata columns} to convert to DQ library's types.
     */
    public static DataType.Type[] convert(List<ColumnMetadata> columns) {
        DataType.Type[] types = new DataType.Type[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            final String type = columns.get(i).getType();
            types[i] = convert(Type.get(type));
        }
        return types;
    }

    public static DataType.Type convert(Type type) {
        switch (type) {
            case ANY:
            case STRING:
                return DataType.Type.STRING;
            case NUMERIC:
                return DataType.Type.INTEGER;
            case INTEGER:
                return DataType.Type.INTEGER;
            case DOUBLE:
            case FLOAT:
                return DataType.Type.DOUBLE;
            case BOOLEAN:
                return DataType.Type.BOOLEAN;
            case DATE:
                return DataType.Type.DATE;
            default:
                return DataType.Type.STRING;
        }

    }

    public static DataType.Type[] convert(Type[] types) {
        DataType.Type[] converted = new DataType.Type[types.length];
        for (int i = 0; i < types.length; i++) {
            converted[i] = convert(types[i]);
        }
        return converted;
    }

    /**
     * @param semanticType A {@link SemanticType semantic type} as returned by the DQ's {@link org.talend.dataquality.semantic.statistics.SemanticAnalyzer}.
     * @return A display name for the semantic type's suggested category or empty string if none found.
     */
    public static String getDomainLabel(SemanticType semanticType) {
        if (semanticType == null) {
            return StringUtils.EMPTY;
        } else {
            return getDomainLabel(semanticType.getSuggestedCategory());
        }
    }

    /**
     * @param categoryId A category id from supported {@link SemanticCategoryEnum categories}.
     * @return A display name for the category id or empty string if none found.
     * @see SemanticCategoryEnum
     */
    public static String getDomainLabel(String categoryId) {
        if (categoryId == null) {
            return StringUtils.EMPTY;
        } else {
            final SemanticCategoryEnum category = SemanticCategoryEnum.getCategoryById(categoryId.toUpperCase());
            return category == null ? StringUtils.EMPTY : category.getDisplayName();
        }
    }
}
