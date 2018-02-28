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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.talend.dataprep.async.conditional.ConditionalTest;

/**
 * A utility class for annotations.
 */
public class AnnotationUtils {

    private AnnotationUtils() {
    }

    /**
     * Returns the id of the parameter annotated with <code>annotationClass</code> in all the <code>pjp</code>
     * arguments. If multiple parameters hold the annotation, returns the index of the <b>last</b> parameter.
     *
     * @param pjp The {@link ProceedingJoinPoint} to check for annotation in parameters.
     * @param annotationClass The annotation to look for.
     * @return The index of the annotated parameter or -1 if not found.
     */
    public static int getAnnotatedParameterIndex(ProceedingJoinPoint pjp, Class<? extends Annotation> annotationClass) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();

        Annotation[][] pa = m.getParameterAnnotations();
        int idParameterIndex = -1;
        int i = 0;
        for (Annotation[] annotations : pa) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    idParameterIndex = i;
                }
            }
            i++;
        }
        return idParameterIndex;
    }

    public static List<Integer> getAnnotatedParameterIndexes(ProceedingJoinPoint pjp, Class<? extends Annotation> annotationClass) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();

        Annotation[][] pa = m.getParameterAnnotations();
        List<Integer> idParameterIndexes = new ArrayList<>();

        int i = 0;
        for (Annotation[] annotations : pa) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    idParameterIndexes.add(i);
                }
            }
            i++;
        }

        return idParameterIndexes;
    }

    public static Object[] extractAsyncParameter(ProceedingJoinPoint pjp) {
        List<Integer> conditionArgIndex = AnnotationUtils.getAnnotatedParameterIndexes(pjp, AsyncParameter.class);

        List<Object> conditionArg = new ArrayList<>();

        conditionArgIndex.forEach( (i) -> {
            conditionArg.add(pjp.getArgs()[i]);
        });

        return conditionArg.toArray(new Object[conditionArg.size()]);
    }
}
