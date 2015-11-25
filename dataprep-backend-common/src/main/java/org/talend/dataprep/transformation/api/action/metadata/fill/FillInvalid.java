package org.talend.dataprep.transformation.api.action.metadata.fill;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillInvalid.FILL_INVALID_ACTION_NAME)
public class FillInvalid extends AbstractFillWith implements ColumnAction {

    public static final String FILL_INVALID_BOOLEAN = "fillinvalidwithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_INVALID_DATE = "fillinvalidwithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_INVALID_NUMERIC = "fillinvalidwithdefaultnumeric"; //$NON-NLS-1$

    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefault"; //$NON-NLS-1$

     public FillInvalid() {
        this(Type.STRING);
    }

    public FillInvalid(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return FILL_INVALID_ACTION_NAME;
    }

    @Override
    public String getDescription() {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_BOOLEAN + ".desc");
        } else if (Type.DATE.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_DATE + ".desc");
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_NUMERIC + ".desc");
        } else {
            return MessagesBundle.getString("action." + FILL_INVALID_ACTION_NAME + ".desc");
        }
    }

    @Override
    public String getLabel() {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_BOOLEAN + ".label");
        } else if (Type.DATE.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_DATE + ".label");
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return MessagesBundle.getString("action." + FILL_INVALID_NUMERIC + ".label");
        } else {
            return MessagesBundle.getString("action." + FILL_INVALID_ACTION_NAME + ".label");
        }
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean shouldBeProcessed(String value, ColumnMetadata colMetadata) {
        return ActionMetadataUtils.checkInvalidValue(colMetadata, value);
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.BOOLEAN.isAssignableFrom(Type.get(column.getType())) //
                || Type.DATE.isAssignableFrom(Type.get(column.getType())) //
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType())) //
                || Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null || !acceptColumn(column)) {
            return this;
        }
        return new FillInvalid(Type.valueOf(column.getType().toUpperCase()));
    }
}
