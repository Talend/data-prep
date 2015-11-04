package org.talend.dataprep.transformation.api.action.metadata.fillempty;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.transformation.api.action.metadata.date.DatePattern;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillIfEmpty.FILL_EMPTY_ACTION_NAME)
public class FillIfEmpty extends ActionMetadata implements ColumnAction {

    public static final String FILL_EMPTY_ACTION_NAME = "fillemptywithdefault";

    public static final String FILL_EMPTY_BOOLEAN = "fillemptywithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_EMPTY_DATE = "fillemptywithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_EMPTY_INTEGER = "fillemptywithdefaultinteger"; //$NON-NLS-1$

    public static final String FILL_EMPTY_STRING = "fillemptywithdefault"; //$NON-NLS-1$

    private static final String DEFAULT_VALUE_PARAMETER = "empty_default_value"; //$NON-NLS-1$

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String DEFAULT_DATE_VALUE = DEFAULT_FORMATTER.format(LocalDateTime.of(1970, Month.JANUARY, 1, 10, 0));

    /**
     * Component that parses dates.
     */
    private final DateParser dateParser = new DateParser(); // TODO investigate why this instantiation is required,
                                                            // should be auto with Autowired

    private final Type type;

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
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        switch (type) {
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case STRING:
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, //
                    ParameterType.STRING, //
                    StringUtils.EMPTY));
            break;
        case INTEGER:
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, //
                    ParameterType.INTEGER, //
                    "0"));
            break;
        case BOOLEAN:
            parameters.add(SelectParameter.Builder.builder() //
                    .name(DEFAULT_VALUE_PARAMETER) //
                    .item("True") //
                    .item("False") //
                    .defaultValue("True") //
                    .build());
            break;
        case DATE:
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, //
                    ParameterType.DATE, //
                    DEFAULT_DATE_VALUE, //
                    false, //
                    false));
            break;
        case ANY:
        default:
            break;
        }
        return parameters;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);
        if (value == null || value.trim().length() == 0) {
            if (type.equals(Type.DATE)) {
                final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
                final LocalDateTime date = dateParser.parse(parameters.get(DEFAULT_VALUE_PARAMETER), columnMetadata);
                final DatePattern mostFrequentPattern = dateParser.getMostFrequentPattern(columnMetadata);
                DateTimeFormatter ourNiceFormatter = (mostFrequentPattern == null ? DEFAULT_FORMATTER : mostFrequentPattern
                        .getFormatter());
                row.set(columnId, ourNiceFormatter.format(date));
            } else {
                row.set(columnId, parameters.get(DEFAULT_VALUE_PARAMETER));
            }
        }
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
