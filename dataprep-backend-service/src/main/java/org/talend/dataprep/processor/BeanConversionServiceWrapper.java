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

package org.talend.dataprep.processor;

import org.talend.dataprep.conversions.BeanConversionService;

/**
 * A super class for all configurations that registers conversions to {@link BeanConversionService}.
 */
public abstract class BeanConversionServiceWrapper implements Wrapper<BeanConversionService> {

    @Override
    public Class<BeanConversionService> wrapped() {
        return BeanConversionService.class;
    }
}
