package org.talend.dataprep.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Class uses to have a have a single ComponentScan for all DataPrep spring applications.
 */
@ComponentScan( //
        value = { "org.talend.dataprep", "org.talend.daikon.content",  "org.talend.daikon.security" }, //
        // Currently we have two controllers for the same entry point '/docs' one from daikon, another one from dataprep.
        // We filter the daikon one. It's a workaround before daikon documentation controller will be optional.
        excludeFilters = @ComponentScan.Filter( //
                value = org.talend.daikon.documentation.DocumentationController.class, //
                type = FilterType.ASSIGNABLE_TYPE //
        ) //
)
public class DataPrepComponentScanConfiguration {
}
