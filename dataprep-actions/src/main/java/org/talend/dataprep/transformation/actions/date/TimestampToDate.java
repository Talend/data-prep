// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.date;

import static java.util.Collections.singletonList;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

@Action(TimestampToDate.ACTION_NAME)
public class TimestampToDate extends AbstractDate implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "timestamp_to_date"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    private static final String APPENDIX = "_as_date"; //$NON-NLS-1$

    private static final boolean CREATE_NEW_COLUMN_DEFAULT_VALUE = true;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DATE.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT_VALUE));
        parameters.addAll(getParametersForDatePattern(locale));
        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        Type result;
        if ("custom".equals(context.getParameters().get(NEW_PATTERN))) {
            // Custom pattern might not be detected as a valid date, create the new column as string for the most
            // permissive type detection.
            result = Type.STRING;
        } else {
            result = Type.DATE;
        }
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + APPENDIX).withType(result));
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT_VALUE)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        compileDatePattern(context);
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();

        // create new column and append it after current column
        final String newColumn = ActionsUtils.getTargetColumnId(context);

        final String value = row.get(columnId);
        row.set(newColumn, getTimeStamp(value, context.<DatePattern> get(COMPILED_DATE_PATTERN).getFormatter()));
    }

    protected String getTimeStamp(String from, DateTimeFormatter dateTimeFormatter) {
        if (!NumericHelper.isBigDecimal(from)) {
            // empty value if the date cannot be parsed
            return StringUtils.EMPTY;
        }
        LocalDateTime date = LocalDateTime.ofEpochSecond(Long.parseLong(from), 0, ZoneOffset.UTC);
        return dateTimeFormatter.format(date);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
