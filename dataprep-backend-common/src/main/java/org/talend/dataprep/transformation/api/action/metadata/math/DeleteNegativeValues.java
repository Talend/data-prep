package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.talend.dataprep.api.type.Type.NUMERIC;
import static org.talend.dataprep.api.type.Type.STRING;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.delete.AbstractDelete;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

/**
 * Delete row on a given value.
 */
@Component(DeleteNegativeValues.ACTION_BEAN_PREFIX + DeleteNegativeValues.ACTION_NAME)
public class DeleteNegativeValues extends AbstractDelete {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "delete_negative_values"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    /**
     * @see AbstractDelete#toDelete(ColumnMetadata, Map, String)
     */
    @Override
    public boolean toDelete(ColumnMetadata colMetadata, Map<String, String> parsedParameters, String value) {
        if (value == null) {
            return false;
        }
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.compareTo(BigDecimal.ZERO) < 0;
        }
        catch (NumberFormatException exc){
            return false;
        }
    }

}
