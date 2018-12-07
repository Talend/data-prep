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

package org.talend.dataprep.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.StringUtils;

/**
 * Identify a method which duration should be log.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD })
public @interface LogTimed {

    String logStartAspectMessage() default StringUtils.EMPTY;

    String logStopAspectMessage() default StringUtils.EMPTY;

    String logSpecificMessage() default StringUtils.EMPTY;

    boolean needLogCorrelationId() default false;

}
