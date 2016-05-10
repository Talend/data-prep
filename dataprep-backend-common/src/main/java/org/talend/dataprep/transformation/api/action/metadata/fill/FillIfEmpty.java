//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.fill;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillIfEmpty.FILL_EMPTY_ACTION_NAME)
@Scope(value = "prototype")
public class FillIfEmpty extends AbstractFillWith implements ColumnAction {

    public static final String FILL_EMPTY_ACTION_NAME = "fillemptywithdefault";

    public static final String FILL_EMPTY_BOOLEAN = "fillemptywithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_EMPTY_DATE = "fillemptywithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_EMPTY_INTEGER = "fillemptywithdefaultinteger"; //$NON-NLS-1$

    public static final String FILL_EMPTY_STRING = "fillemptywithdefault"; //$NON-NLS-1$

    @Autowired
    private ApplicationContext applicationContext;

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
            return getMessagesBundle().getString("action." + FILL_EMPTY_STRING + ".desc");
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return getMessagesBundle().getString("action." + FILL_EMPTY_INTEGER + ".desc");
        case BOOLEAN:
            return getMessagesBundle().getString("action." + FILL_EMPTY_BOOLEAN + ".desc");
        case DATE:
            return getMessagesBundle().getString("action." + FILL_EMPTY_DATE + ".desc");
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }
    }

    @Override
    public String getLabel() {
        switch (type) {
        case STRING:
            return getMessagesBundle().getString("action." + FILL_EMPTY_STRING + ".label");
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return getMessagesBundle().getString("action." + FILL_EMPTY_INTEGER + ".label");
        case BOOLEAN:
            return getMessagesBundle().getString("action." + FILL_EMPTY_BOOLEAN + ".label");
        case DATE:
            return getMessagesBundle().getString("action." + FILL_EMPTY_DATE + ".label");
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
        return applicationContext.getBean( getClass(), Type.valueOf(column.getType().toUpperCase()));
    }
}
