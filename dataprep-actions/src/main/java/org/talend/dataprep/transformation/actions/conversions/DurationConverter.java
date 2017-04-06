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

import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter.Builder;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DurationConverter.ACTION_NAME)
public class DurationConverter extends AbstractActionMetadata implements ColumnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DurationConverter.class);

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "duration_converter";

    /**
     * Action parameters:
     */

    protected static final String FROM_UNIT_PARAMETER = "from_unit";

    protected static final String TO_UNIT_PARAMETER = "to_unit";

    /**
     * Converter help class.
     */
    private static final String CONVERTER_HELPER = "converter_helper";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.CONVERSIONS.getDisplayName();
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        Builder builder = Builder.builder()
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
                .build());
        
        parameters.add(builder
                .name(TO_UNIT_PARAMETER)
                .defaultValue(ChronoUnit.HOURS.name()) 
                .build());
        
        //@formatter:on
        return ActionsBundle.attachToAction(parameters, this);
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
        if (NumberUtils.isNumber(colValue)) {
            double value = BigDecimalParser.toBigDecimal(colValue).doubleValue();
            final org.talend.dataquality.converters.DurationConverter converter = context.get(CONVERTER_HELPER);
            row.set(columnId, String.valueOf(converter.convert(value)));
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
