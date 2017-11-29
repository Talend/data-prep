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
        String mod = Double.toString(BigDecimalParser.toBigDecimal(columnValue).doubleValue());

        if (StringUtils.isNotBlank(parameter)) {
            mod = BigDecimalParser
                    .toBigDecimal(columnValue)
                    .remainder(BigDecimalParser.toBigDecimal(parameter))
                    .abs()
                    .toString();
        }
        return mod;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
