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

package org.talend.dataprep.async;

import java.lang.annotation.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.talend.dataprep.async.conditional.AlwaysTrueCondition;
import org.talend.dataprep.async.conditional.ConditionalTest;
import org.talend.dataprep.async.generator.AnnotationExecutionIdGenerator;
import org.talend.dataprep.async.generator.AnnotationGroupIdGenerator;
import org.talend.dataprep.async.generator.ExecutionIdGenerator;
import org.talend.dataprep.async.result.EmptyUrlGenerator;
import org.talend.dataprep.async.result.ResultUrlGenerator;

/**
 * Annotation used to declare that the underlying operation should be ran using a asynchronous executor.
 *
 * Asynchronous execution can optionally be grouped together, please, have a look at
 * {@link GroupIdGenerator} or {@link AsyncGroupId}.
 *
 * @see GroupIdGenerator
 * @see AsyncGroupId
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncOperation {

    /**
     * Spring bean name used to provide the the asynchronous operation group id.
     *
     * The bean name is tried before the class attribute. So if both the bean and the class are set, the bean has
     * priority over the class.
     *
     * @return the spring bean name that provides the group id generator for this async operation.
     */
    Class<? extends GroupIdGenerator> groupIdGeneratorBean() default DEFAULT.class;

    /**
     * Class used to get the async operation group id.
     *
     * @return The class used to get the async operation group id.
     */
    Class<? extends GroupIdGenerator> groupIdGeneratorClass() default AnnotationGroupIdGenerator.class;

    /**
     * Class used to get the async operation id.
     *
     * @return The class used to get the async operation id.
     */
    Class<? extends ExecutionIdGenerator> executionIdGeneratorClass() default AnnotationExecutionIdGenerator.class;

    /**
     * Class used to test if we need to execute the method asynchronously
     *
     * @return The class used to test if we need to execute the method asynchronously
     */

    Class<? extends ConditionalTest> conditionalClass() default AlwaysTrueCondition.class;

    /**
     * Class used to generate the URL used to get the final result
     * @return The class used to generate the URL used to get the final result
     */
    Class<? extends ResultUrlGenerator> resultUrlGenerator() default EmptyUrlGenerator.class;

    /**
     * Default class needed to set a default value to the groupIdGeneratorBean.
     */
    class DEFAULT implements GroupIdGenerator {

        @Override
        public String getGroupId(ProceedingJoinPoint pjp) {
            return null;
        }
    }
}
