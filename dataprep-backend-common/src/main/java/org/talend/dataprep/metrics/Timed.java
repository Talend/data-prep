package org.talend.dataprep.metrics;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD })
public @interface Timed {
}
