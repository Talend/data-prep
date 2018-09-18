// ============================================================================
//
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

package org.talend.dataprep.units;

import java.math.BigDecimal;
import java.math.MathContext;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

public class TemperatureImpl implements javax.measure.quantity.Temperature {

    private final BigDecimal value;

    private final Unit<javax.measure.quantity.Temperature> tempUnit;

    public TemperatureImpl(BigDecimal value, Unit<javax.measure.quantity.Temperature> unit) {
        this.value = value;
        this.tempUnit = unit;
    }

    public TemperatureImpl convertTo(Unit<javax.measure.quantity.Temperature> targetUnit) {
        UnitConverter converter = tempUnit.getConverterTo(targetUnit);
        BigDecimal convertedValue = converter.convert(value, MathContext.DECIMAL128);
        return new TemperatureImpl(convertedValue, targetUnit);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Unit<javax.measure.quantity.Temperature> getUnit() {
        return tempUnit;
    }
}
