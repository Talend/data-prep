package org.talend.dataprep.transformation.api.action.metadata.text;

import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + ProperCase.PROPER_CASE_ACTION_NAME)
public class ProperCase extends ActionMetadata implements ColumnAction {

    public static final String PROPER_CASE_ACTION_NAME = "propercase"; //$NON-NLS-1$

    @Override
    public String getName() {
        return PROPER_CASE_ACTION_NAME;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String toProperCase = row.get(columnId);
        if (toProperCase != null) {
            row.set(columnId, WordUtils.capitalizeFully(toProperCase));
        }
    }
}
