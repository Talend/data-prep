// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.text;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ComputeLength.LENGTH_ACTION_NAME)
public class ComputeLength extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String LENGTH_ACTION_NAME = "compute_length"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_length"; //$NON-NLS-1$

    @Override
    public String getName() {
        return LENGTH_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    protected boolean createNewColumnParamVisible() {
        return false;
    }

    @Override
    public boolean getCreateNewColumnDefaultValue() {
        return true;
    }

    @Override
    public Type getColumnType(ActionContext context){
        return Type.INTEGER;
    }

    @Override
    public String getCreatedColumnName(ActionContext context){
        return context.getColumnName() + APPENDIX;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // create new column and append it after current column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String lengthColumn = getTargetColumnId(context);

        // Set length value
        final String value = row.get(columnId);
        row.set(lengthColumn, value == null ? "0" : String.valueOf(value.length()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
