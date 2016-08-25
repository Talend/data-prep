package org.talend.dataprep.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

public class AspectHelper {

    private AspectHelper() {
    }

    public static <T> T getAnnotation(ProceedingJoinPoint pjp, Class<T> annotationClass) {
        final MethodSignature methodSignature = ((MethodSignature) pjp.getSignature());
        final Annotation[] annotations = methodSignature.getMethod().getAnnotations();
        T lookupAnnotation = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                lookupAnnotation = (T) annotation;
                break;
            }
        }
        return lookupAnnotation;
    }
}
