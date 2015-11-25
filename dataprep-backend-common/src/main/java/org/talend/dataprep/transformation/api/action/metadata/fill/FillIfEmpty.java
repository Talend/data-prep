package org.talend.dataprep.transformation.api.action.metadata.fill;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillIfEmpty.FILL_EMPTY_ACTION_NAME)
public class FillIfEmpty extends AbstractFillWith implements ColumnAction {

    public static final String FILL_EMPTY_ACTION_NAME = "fillemptywithdefault";

    public static final String FILL_EMPTY_BOOLEAN = "fillemptywithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_EMPTY_DATE = "fillemptywithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_EMPTY_INTEGER = "fillemptywithdefaultinteger"; //$NON-NLS-1$

    public static final String FILL_EMPTY_STRING = "fillemptywithdefault"; //$NON-NLS-1$

    public FillIfEmpty() {
        this(Type.STRING);
    }

    public FillIfEmpty(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return FILL_EMPTY_ACTION_NAME;
    }

    @Override
    public String getDescription() {
        switch (type) {
        case STRING:
            return MessagesBundle.getString("action." + FILL_EMPTY_STRING + ".desc");
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return MessagesBundle.getString("action." + FILL_EMPTY_INTEGER + ".desc");
        case BOOLEAN:
            return MessagesBundle.getString("action." + FILL_EMPTY_BOOLEAN + ".desc");
        case DATE:
            return MessagesBundle.getString("action." + FILL_EMPTY_DATE + ".desc");
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }
    }

    @Override
    public String getLabel() {
        switch (type) {
        case STRING:
            return MessagesBundle.getString("action." + FILL_EMPTY_STRING + ".label");
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return MessagesBundle.getString("action." + FILL_EMPTY_INTEGER + ".label");
        case BOOLEAN:
            return MessagesBundle.getString("action." + FILL_EMPTY_BOOLEAN + ".label");
        case DATE:
            return MessagesBundle.getString("action." + FILL_EMPTY_DATE + ".label");
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }

    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean shouldBeProcessed(String value, ColumnMetadata colMetadata) {
        return (value == null || value.trim().length() == 0);
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.BOOLEAN.equals(Type.get(column.getType())) //
                || Type.DATE.equals(Type.get(column.getType())) //
                || Type.INTEGER.equals(Type.get(column.getType())) //
                || Type.DOUBLE.equals(Type.get(column.getType())) //
                || Type.FLOAT.equals(Type.get(column.getType())) //
                || Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null || !acceptColumn(column)) {
            return this;
        }
        return new FillIfEmpty(Type.valueOf(column.getType().toUpperCase()));
    }
}
