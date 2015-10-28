package org.talend.dataprep.transformation.api.action.metadata.text;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(TextClustering.ACTION_BEAN_PREFIX + TextClustering.TEXT_CLUSTERING)
public class TextClustering extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TEXT_CLUSTERING = "textclustering";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TEXT_CLUSTERING;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName();
    }

    /**
     * @see ActionMetadata#isDynamic()
     */
    @Override
    public boolean isDynamic() {
        return true;
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

        // replace only the value if present in parameters
        final String replaceValue = parameters.get(value);
        if (replaceValue != null) {
            row.set(columnId, replaceValue);
        }
    }
}
