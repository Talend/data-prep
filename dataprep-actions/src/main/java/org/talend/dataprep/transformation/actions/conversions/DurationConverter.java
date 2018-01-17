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

package org.talend.dataprep.transformation.actions.conversions;

import static java.time.temporal.ChronoUnit.*;
import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.DOUBLE;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter.SelectParameterBuilder;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(DurationConverter.ACTION_NAME)
public class DurationConverter extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "duration_converter";

    /**
     * Action parameters:
     */

    private static final String FROM_UNIT_PARAMETER = "from_unit";

    private static final String TO_UNIT_PARAMETER = "to_unit";

    private static final String TARGET_PRECISION = "precision";

    private static final String NEW_COLUMN_SEPARATOR = "_in_";

    /**
     * Converter help class.
     */
    private static final String CONVERTER_HELPER = "converter_helper";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.CONVERSIONS.getDisplayName(locale);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn()
                .withName(context.getColumnName() + NEW_COLUMN_SEPARATOR + context.getParameters().get(TO_UNIT_PARAMETER))
                .withType(DOUBLE));
    }

    @Override
        public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        List<ChronoUnit> chronoUnits = Stream
                .of(YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLIS)
                .collect(Collectors.toList());

        //@formatter:off
        SelectParameterBuilder builderFrom = selectParameter(locale)
                .name(FROM_UNIT_PARAMETER);
        for (ChronoUnit unit : chronoUnits) {
            builderFrom = builderFrom.constant(unit.name(), unit.toString());
        }
        builderFrom = builderFrom.defaultValue(DAYS.name());
        parameters.add(builderFrom.build(this));

        SelectParameterBuilder builderTo = selectParameter(locale)
                .name(TO_UNIT_PARAMETER);
        for (ChronoUnit unit : chronoUnits) {
            builderTo = builderTo.constant(unit.name(), unit.toString());
        }
        builderTo = builderTo.defaultValue(HOURS.name());
        parameters.add(builderTo.build(this));

         parameters.add(parameter(locale).setName(TARGET_PRECISION).setType(INTEGER).setDefaultValue("1").setPlaceHolder("precision").build(this));

        //@formatter:on
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
        if (actionContext.getActionStatus() == OK) {
            ChronoUnit fromUnit = valueOf(actionContext.getParameters().get(FROM_UNIT_PARAMETER));
            ChronoUnit toUnit = valueOf(actionContext.getParameters().get(TO_UNIT_PARAMETER));
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

            row.set(ActionsUtils.getTargetColumnId(context), valueToString);
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
