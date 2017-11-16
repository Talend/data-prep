// ============================================================================
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

package org.talend.dataprep.transformation.actions.conversions;

import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.parameters.SelectParameter.SelectParameterBuilder;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DurationConverter.ACTION_NAME)
public class DurationConverter extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "duration_converter";

    /**
     * Action parameters:
     */

    protected static final String FROM_UNIT_PARAMETER = "from_unit";

    protected static final String TO_UNIT_PARAMETER = "to_unit";

    protected static final String TARGET_PRECISION = "precision";

    /**
     * Converter help class.
     */
    private static final String CONVERTER_HELPER = "converter_helper";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.CONVERSIONS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        //@formatter:off
        SelectParameterBuilder builder = SelectParameter.selectParameter(locale)
                .item(ChronoUnit.YEARS.name(), ChronoUnit.YEARS.toString())
                .item(ChronoUnit.MONTHS.name(), ChronoUnit.MONTHS.toString())
                .item(ChronoUnit.WEEKS.name(), ChronoUnit.WEEKS.toString())
                .item(ChronoUnit.DAYS.name(), ChronoUnit.DAYS.toString())
                .item(ChronoUnit.HOURS.name(), ChronoUnit.HOURS.toString())
                .item(ChronoUnit.MINUTES.name(), ChronoUnit.MINUTES.toString())
                .item(ChronoUnit.SECONDS.name(), ChronoUnit.SECONDS.toString())
                .item(ChronoUnit.MILLIS.name(), ChronoUnit.MILLIS.toString())
                .canBeBlank(false);

        parameters.add(builder
                .name(FROM_UNIT_PARAMETER)
                .defaultValue(ChronoUnit.DAYS.name())
                .build(this ));

        parameters.add(builder
                .name(TO_UNIT_PARAMETER)
                .defaultValue(ChronoUnit.HOURS.name())
                .build(this ));

         parameters.add(Parameter.parameter(locale).setName(TARGET_PRECISION).setType(INTEGER).setDefaultValue("1").setPlaceHolder("precision").build(this));

        //@formatter:on
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            ChronoUnit fromUnit = ChronoUnit.valueOf(actionContext.getParameters().get(FROM_UNIT_PARAMETER));
            ChronoUnit toUnit = ChronoUnit.valueOf(actionContext.getParameters().get(TO_UNIT_PARAMETER));
            actionContext.get(CONVERTER_HELPER, p -> new org.talend.dataquality.converters.DurationConverter(fromUnit, toUnit));
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        if (!StringUtils.isEmpty(colValue)) {
            String valueToString;
            try {
                final org.talend.dataquality.converters.DurationConverter converter = context.get(CONVERTER_HELPER);
                BigDecimal valueFrom = BigDecimalParser.toBigDecimal(colValue);
                if (Double.isInfinite(valueFrom.doubleValue())) {
                    valueToString = colValue;
                } else {
                    double valueTo = converter.convert(valueFrom.doubleValue());
                    String precisionParameter = context.getParameters().get(TARGET_PRECISION);
                    Integer targetScale = NumberUtils.toInt(precisionParameter, valueFrom.scale());
                    valueToString = BigDecimalParser.toBigDecimal(String.valueOf(valueTo)).setScale(targetScale, RoundingMode.HALF_UP).toPlainString();
                }
            } catch (NumberFormatException nfe) {
                valueToString = colValue;
            }

            row.set(columnId, valueToString);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.dataprep.transformation.actions.common.AbstractActionMetadata#acceptField(org.talend.dataprep.api.dataset.
     * ColumnMetadata)
     */
    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.NUMERIC.isAssignableFrom(column.getType());
    }

}
