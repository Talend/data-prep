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
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TemperatureImplUnitTest {

    @Test
    public void convert_fahrenheitToCelsiusToFahrenheit() throws Exception {
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("-40"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("-40"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("5"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("-15"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("41"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("5"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("77"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("25"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("113"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("45"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("158"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("70"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("185"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("85"), SI.CELSIUS));
        assertTemperaturesAreEqual(new TemperatureImpl(new BigDecimal("203"), NonSI.FAHRENHEIT),
                new TemperatureImpl(new BigDecimal("95"), SI.CELSIUS));
    }

    private void assertTemperaturesAreEqual(TemperatureImpl temp1, TemperatureImpl temp2) {
        TemperatureImpl convertedTemp = temp1.convertTo(temp2.getUnit());
        assertEquals(temp2.getUnit(), convertedTemp.getUnit());
        assertEquals(temp2.getValue().doubleValue(), convertedTemp.getValue().doubleValue(), 20);

        TemperatureImpl convertedTempOtherWay = temp2.convertTo(temp1.getUnit());
        assertEquals(temp1.getUnit(), convertedTempOtherWay.getUnit());
        assertEquals(temp1.getValue().doubleValue(), convertedTempOtherWay.getValue().doubleValue(), 20);
    }

}