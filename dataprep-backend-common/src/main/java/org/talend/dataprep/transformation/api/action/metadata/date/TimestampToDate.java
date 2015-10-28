package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

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

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {

        DatePattern newPattern = getDateFormat(parameters);

        // create new column and append it after current column
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String newColumn = rowMetadata.insertAfter(columnId, createNewColumn(column, parameters));

        final String value = row.get(columnId);
        row.set(newColumn, getTimeStamp(value, newPattern.getFormatter()));
    }

    protected String getTimeStamp(String from, DateTimeFormatter dateTimeFormatter) {
        try {
            LocalDateTime date = LocalDateTime.ofEpochSecond(Long.parseLong(from), 0, ZoneOffset.UTC);
            return dateTimeFormatter.format(date);
        } catch (NumberFormatException e) {
            // empty value if the date cannot be parsed
            return "";
        }
    }

    /**
     * Create the new "date" column.
     *
     * @param column the current column metadata.
     * @param parameters action parameters, used to detect if a custom date format is being used.
     * @return the new column metadata
     */
    private ColumnMetadata createNewColumn(final ColumnMetadata column, Map<String, String> parameters) {
        final Type type;
        if ("custom".equals(parameters.get(NEW_PATTERN))) {
            // Custom pattern might not be detected as a valid date, create the new column as string for the most
            // permissive type detection.
            type = Type.STRING;
        } else {
            type = Type.DATE;
        }
        return ColumnMetadata.Builder //
                .column() //
                .name(column.getName() + APPENDIX) //
                .type(type) //
                .headerSize(column.getHeaderSize()) //
                .build();
    }

}
