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

package org.talend.dataprep.transformation.actions.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(Modulo.MODULO_NAME)
public class Modulo extends AbstractMathOneParameterAction {

    protected static final String MODULO_NAME = "modulo";

    @Override
    public String getName() {
        return MODULO_NAME;
    }

    @Override
    protected String getSuffix(ActionContext context) {
        return "_mod";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);
        BigDecimal mod = BigDecimalParser.toBigDecimal(parameter);
        if (StringUtils.isNotBlank(parameter)) {
            value = modulo(value, mod);
        }
        return value.toPlainString();
    }

    // TODO: Must be private as only visible for test purpose
    BigDecimal modulo(BigDecimal value, BigDecimal mod) {

        final int scale = ConstantUtilMath.SCALE_PRECISION;
        final RoundingMode rm = ConstantUtilMath.ROUNDING_MODE;

        BigDecimal result = value.remainder(mod);
        if (result.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else if (value.compareTo(BigDecimal.ZERO) < 0) {
            if (mod.compareTo(BigDecimal.ZERO) > 0) {
                result = result.add(mod);
            }
        } else if (mod.compareTo(BigDecimal.ZERO) < 0) {
            result = result.add(mod);
        }
        return result.setScale(scale, rm).stripTrailingZeros();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
