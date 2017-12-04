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
package org.talend.dataprep.transformation.actions.conversions;

import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.DOUBLE;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.converters.DistanceEnum;

/**
 * Convert distance from one unit to another.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DistanceConverter.ACTION_NAME)
public class DistanceConverter extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "distance_converter";

    private static final String FROM_UNIT_PARAMETER = "from_unit";

    private static final String TO_UNIT_PARAMETER = "to_unit";

    private static final String TARGET_PRECISION = "precision";

    protected static final String NEW_COLUMN_SEPARATOR = "_in_";

    /**
     * @return The list of parameters required for this Action to be executed.
     *
     * @param locale*/
    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        SelectParameter.SelectParameterBuilder builder = SelectParameter.selectParameter(locale)
                .item(DistanceEnum.MILLIMETER.name(), DistanceEnum.MILLIMETER.getShortName())
                .item(DistanceEnum.CENTIMETER.name(), DistanceEnum.CENTIMETER.getShortName())
                .item(DistanceEnum.DECIMETER.name(), DistanceEnum.DECIMETER.getShortName())
                .item(DistanceEnum.METER.name(), DistanceEnum.METER.getShortName())
                .item(DistanceEnum.DEKAMETER.name(), DistanceEnum.DEKAMETER.getShortName())
                .item(DistanceEnum.HECTOMETER.name(), DistanceEnum.HECTOMETER.getShortName())
                .item(DistanceEnum.KILOMETER.name(), DistanceEnum.KILOMETER.getShortName())
                .item(DistanceEnum.INCH.name(), DistanceEnum.INCH.getShortName())
                .item(DistanceEnum.FOOT.name(), DistanceEnum.FOOT.getShortName())
                .item(DistanceEnum.YARD.name(), DistanceEnum.YARD.getShortName())
                .item(DistanceEnum.MILE.name(), DistanceEnum.MILE.getShortName())
                .item(DistanceEnum.NAUTICAL_MILE.name(), DistanceEnum.NAUTICAL_MILE.getShortName())
                .item(DistanceEnum.LIGHT_YEAR.name(), DistanceEnum.LIGHT_YEAR.getShortName())
                .canBeBlank(false);

        parameters.add(builder
                .defaultValue(DistanceEnum.MILE.name())
                .name(FROM_UNIT_PARAMETER)
                .build(this));

        parameters.add(builder
                .defaultValue(DistanceEnum.KILOMETER.name())
                .name(TO_UNIT_PARAMETER)
                .build(this));

        parameters.add(Parameter.parameter(locale).setName(TARGET_PRECISION)
                .setType(INTEGER)
                .setDefaultValue("2")
                .setPlaceHolder("precision")
                .build(this));

        return parameters;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.CONVERSIONS.getDisplayName(locale);
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(new AdditionalColumn(DOUBLE,
                context.getColumnName() + NEW_COLUMN_SEPARATOR + context.getParameters().get(TO_UNIT_PARAMETER)));
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.NUMERIC.isAssignableFrom(column.getType());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            DistanceEnum unitFrom = DistanceEnum.valueOf(actionContext.getParameters().get(FROM_UNIT_PARAMETER));
            DistanceEnum unitTo = DistanceEnum.valueOf(actionContext.getParameters().get(TO_UNIT_PARAMETER));
            org.talend.dataquality.converters.DistanceConverter converter = new org.talend.dataquality.converters.DistanceConverter(unitFrom, unitTo);
            actionContext.get(ACTION_NAME, parameters -> converter);
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String columnValue = row.get(columnId);

        if (!StringUtils.isEmpty(columnValue)) {
            String valueToString;
            try {
                final org.talend.dataquality.converters.DistanceConverter converter = context.get(ACTION_NAME);
                BigDecimal valueFrom = BigDecimalParser.toBigDecimal(columnValue);
                if (Double.POSITIVE_INFINITY == valueFrom.doubleValue() || Double.NEGATIVE_INFINITY == valueFrom.doubleValue()) {
                    valueToString = columnValue;
                } else {
                    double valueTo = converter.convert(valueFrom.doubleValue());
                    String precisionParameter = context.getParameters().get(TARGET_PRECISION);
                    Integer targetScale = NumberUtils.toInt(precisionParameter, valueFrom.scale());
                    valueToString = BigDecimalParser.toBigDecimal(String.valueOf(valueTo)).setScale(targetScale, RoundingMode.HALF_UP).toPlainString();
                }
            } catch (NumberFormatException nfe) {
                valueToString = columnValue;
            }

            row.set(getTargetColumnId(context), valueToString);
        }
    }
}
