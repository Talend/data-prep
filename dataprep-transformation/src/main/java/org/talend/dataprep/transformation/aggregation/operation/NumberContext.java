//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.aggregation.operation;

import org.talend.dataprep.transformation.aggregation.api.WorkingContext;

/**
 * Simple working context that only wraps a number.
 */
public class NumberContext implements WorkingContext {

    /** The wrapped value. */
    private double value;

    /**
     * Create a Number context with the given value.
     * 
     * @param value the initialization value.
     */
    public NumberContext(double value) {
        this.value = value;
    }

    /**
     * @see WorkingContext#getValue()
     */
    @Override
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set.
     */
    public void setValue(double value) {
        this.value = value;
    }
}
