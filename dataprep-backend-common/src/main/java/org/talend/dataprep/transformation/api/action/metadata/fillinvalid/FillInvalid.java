package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.transformation.api.action.metadata.date.DatePattern;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillInvalid.FILL_INVALID_ACTION_NAME)
public class FillInvalid extends AbstractActionMetadata implements ColumnAction {

    public static final String FILL_INVALID_BOOLEAN = "fillinvalidwithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_INVALID_DATE = "fillinvalidwithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_INVALID_NUMERIC = "fillinvalidwithdefaultnumeric"; //$NON-NLS-1$

    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefault"; //$NON-NLS-1$

    /** Default parameter name. */
    public static final String DEFAULT_VALUE_PARAMETER = "invalid_default_value"; //$NON-NLS-1$

    /**
     * If changing pattern you must change the pattern in the ui as well
     * dataprep-webapp/src/components/transformation/params/date/transformation-date-params.html
     * Yup as usual those bloody Javascript hipsters reinvented the wheel and didn't want to use
     * same pattern as the old school Java guys!!
     */
    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String DEFAULT_DATE_VALUE = DEFAULT_FORMATTER.format(LocalDateTime.of(1970, Month.JANUARY, 1, 10, 0));

    @Autowired
    private DateParser dateParser = new DateParser(); // TODO investigate why this instantiation is required, should be auto with Autowired


    private final Type type;

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
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            parameters.add(SelectParameter.Builder.builder() //
                    .name(DEFAULT_VALUE_PARAMETER) //
                    .item("True") //
                    .item("False") //
                    .defaultValue("True") //
                    .build());
        } else if (Type.DATE.isAssignableFrom(type)) {
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, ParameterType.DATE, DEFAULT_DATE_VALUE, false, false));
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, ParameterType.INTEGER, "0", false, false)); //$NON-NLS-1$
        } else {
            parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, ParameterType.STRING, StringUtils.EMPTY, false, false));
        }
        return parameters;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.BOOLEAN.isAssignableFrom(Type.get(column.getType())) //
                || Type.DATE.isAssignableFrom(Type.get(column.getType())) //
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType())) //
                || Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);

        // olamy do we really consider null as a valid value?
        // note we don't for Date see @FillWithDateIfInvalid
        if (value == null) {
            return;
        }

        final ColumnMetadata colMetadata = row.getRowMetadata().getById(columnId);

        if (ActionMetadataUtils.checkInvalidValue(colMetadata, value)) {
            final String newValue;
            if (Type.DATE.isAssignableFrom(type)) {
                final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
                final LocalDateTime date = dateParser.parse(parameters.get(DEFAULT_VALUE_PARAMETER), columnMetadata);
                final DatePattern mostFrequentPattern = dateParser.getMostFrequentPattern(columnMetadata);
                DateTimeFormatter ourNiceFormatter = (mostFrequentPattern == null ? DEFAULT_FORMATTER : mostFrequentPattern
                        .getFormatter());
                newValue = ourNiceFormatter.format(date);
            } else {
                newValue = parameters.get(DEFAULT_VALUE_PARAMETER);
            }
            row.set(columnId, newValue);
            // update invalid values of column metadata to prevent unnecessary future analysis
            final Set<String> invalidValues = colMetadata.getQuality().getInvalidValues();
            invalidValues.add(value);
        }
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null || !acceptColumn(column)) {
            return this;
        }
        return new FillInvalid(Type.valueOf(column.getType().toUpperCase()));
    }
}
