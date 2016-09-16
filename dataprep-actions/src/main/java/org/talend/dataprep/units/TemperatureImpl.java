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
