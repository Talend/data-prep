package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionScope.EQUALS;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Clear cell when value is equals.
 */
@Component(ClearEquals.ACTION_BEAN_PREFIX + ClearEquals.ACTION_NAME)
public class ClearEquals extends AbstractClear implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_equals"; //$NON-NLS-1$

    public static final String VALUE_PARAMETER = "equals_value"; //$NON-NLS-1$

    private static final List<String> ACTION_SCOPE = Collections.singletonList(EQUALS.getDisplayName());

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
        return DATA_CLEANSING.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#getActionScope()
     */
    @Override
    public List<String> getActionScope() {
        return ACTION_SCOPE;
    }

    public boolean toClear(ColumnMetadata colMetadata, String value, ActionContext context) {
        Map<String, String> parameters = context.getParameters();
        String equalsValue = parameters.get(VALUE_PARAMETER);

        if (StringUtils.equals(value, equalsValue)) {
            return true;
        }
        return false;
    }

}
