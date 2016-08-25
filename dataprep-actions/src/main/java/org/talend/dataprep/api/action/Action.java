package org.talend.dataprep.api.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a class so it is considered as an {@link ActionDefinition definition} to be used in Spring
 * context.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {

    /**
     * @return The unique name for the action
     */
    String value();
}
