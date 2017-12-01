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

package org.talend.dataprep.transformation.actions.math;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;

import java.math.BigDecimal;
import java.util.*;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Modulo.MODULO_NAME)
public class Modulo extends AbstractMathOneParameterAction {

    protected static final String MODULO_NAME = "modulo";

    @Override
    public String getName() {
        return MODULO_NAME;
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "mod";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);
        BigDecimal mod = BigDecimalParser.toBigDecimal(parameter);
        if (StringUtils.isNotBlank(parameter)) {
            value = modulo(value, mod);
        }
        return value.toString();

    }

    protected BigDecimal modulo(BigDecimal value, BigDecimal mod) {
        value = value.remainder(mod);
        if (value.compareTo(BigDecimal.ZERO) == -1) {
            if (mod.compareTo(BigDecimal.ZERO) == 1) {
                value = value.add(mod);
            }
        } else {
            if (mod.compareTo(BigDecimal.ZERO) == -1) {
                value = value.add(mod);
            }
        }
        return value.stripTrailingZeros();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
