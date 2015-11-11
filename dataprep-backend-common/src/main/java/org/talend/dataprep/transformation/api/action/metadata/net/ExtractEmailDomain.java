package org.talend.dataprep.transformation.api.action.metadata.net;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.api.type.Type.STRING;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Split a cell value on a separator.
 */
@Component(ExtractEmailDomain.ACTION_BEAN_PREFIX + ExtractEmailDomain.EXTRACT_DOMAIN_ACTION_NAME)
public class ExtractEmailDomain extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String EXTRACT_DOMAIN_ACTION_NAME = "extractemaildomain"; //$NON-NLS-1$

    /**
     * The local suffix.
     */
    private static final String LOCAL = "_local"; //$NON-NLS-1$

    /**
     * The domain suffix.
     */
    private static final String DOMAIN = "_domain"; //$NON-NLS-1$

    /**
     * Private constructor to ensure IoC use.
     */
    protected ExtractEmailDomain() {
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return EXTRACT_DOMAIN_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.SPLIT.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType())) && StringUtils.equalsIgnoreCase("email", column.getDomain());
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String originalValue = row.get(columnId);
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        // Perform metadata level actions (add local + domain columns).
        final String local = context.in(this).column(
                column.getName() + LOCAL,
                rowMetadata,
                (r) -> {
                    final ColumnMetadata newColumn = column().name(column.getName() + LOCAL).type(Type.STRING).build();
                    rowMetadata.insertAfter(columnId, newColumn);
                    return newColumn;
                }
        );
        final String domain = context.in(this).column(
                column.getName() + DOMAIN,
                rowMetadata,
                (r) -> {
                    final ColumnMetadata newColumn = column().name(column.getName() + DOMAIN).type(Type.STRING).build();
                    rowMetadata.insertAfter(local, newColumn);
                    return newColumn;
                }
        );
        // Set the values in newly created columns
        if (originalValue == null) {
            return;
        }
        final String[] split = originalValue.split("@", 2);
        final String localPart = split.length >= 2 ? split[0] : StringUtils.EMPTY;
        row.set(local, localPart);
        final String domainPart = split.length >= 2 ? split[1] : StringUtils.EMPTY;
        row.set(domain, domainPart);
    }

}
