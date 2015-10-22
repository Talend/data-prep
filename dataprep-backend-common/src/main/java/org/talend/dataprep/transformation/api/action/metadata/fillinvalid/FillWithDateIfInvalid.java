package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.transformation.api.action.metadata.date.DatePattern;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

@Component(value = FillWithDateIfInvalid.ACTION_BEAN_PREFIX + FillWithDateIfInvalid.FILL_INVALID_ACTION_NAME)
public class FillWithDateIfInvalid extends AbstractFillIfInvalid {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FillWithDateIfInvalid.class);

    /** The action name. */
    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefaultdate"; //$NON-NLS-1$

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

    @Override
    public String getName() {
        return FILL_INVALID_ACTION_NAME;
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, ParameterType.DATE, DEFAULT_DATE_VALUE, false, false));
        return parameters;
    }

    @Override
    protected String getDefaultValue(DataSetRow row, Map<String, String> parameters, String columnId) {
        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
        final LocalDateTime date = dateParser.parse(parameters.get(DEFAULT_VALUE_PARAMETER), columnMetadata);
        final DatePattern mostFrequentPattern = dateParser.getMostFrequentPattern(columnMetadata);

        DateTimeFormatter ourNiceFormatter = (mostFrequentPattern == null ? DEFAULT_FORMATTER : mostFrequentPattern
                .getFormatter());

        return ourNiceFormatter.format(date);
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type type = Type.get(column.getType());
        return Type.DATE.equals(type);
    }

}
