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

package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

@Component(TimestampToDate.ACTION_BEAN_PREFIX + TimestampToDate.ACTION_NAME)
public class TimestampToDate extends ActionMetadata implements ColumnAction, DatePatternParamModel {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "timestamp_to_date"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_as_date"; //$NON-NLS-1$

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
        return Type.INTEGER.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = ImplicitParameters.getParameters();
        parameters.addAll(getParametersForDatePattern());
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        compileDatePattern(context);
        // create new column and append it after current column
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        context.column(column.getName() + APPENDIX, (r) -> {
            final Type type;
            if ("custom".equals(parameters.get(NEW_PATTERN))) {
                // Custom pattern might not be detected as a valid date, create the new column as string for the most
                // permissive type detection.
                type = Type.STRING;
            } else {
                type = Type.DATE;
            }
            final ColumnMetadata c = ColumnMetadata.Builder //
                    .column() //
                    .name(column.getName() + APPENDIX) //
                    .type(type) //
                    .headerSize(column.getHeaderSize()) //
                    .build();
            rowMetadata.insertAfter(columnId, c);
            return c;
        });
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();

        // create new column and append it after current column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String newColumn = context.column(column.getName() + APPENDIX);

        final String value = row.get(columnId);
        row.set(newColumn, getTimeStamp(value, context.<DatePattern> get(COMPILED_DATE_PATTERN).getFormatter()));
    }

    protected String getTimeStamp(String from, DateTimeFormatter dateTimeFormatter) {
        try {
            LocalDateTime date = LocalDateTime.ofEpochSecond(Long.parseLong(from), 0, ZoneOffset.UTC);
            return dateTimeFormatter.format(date);
        } catch (NumberFormatException e) {
            // empty value if the date cannot be parsed
            return StringUtils.EMPTY;
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
