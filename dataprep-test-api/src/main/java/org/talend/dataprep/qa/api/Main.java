package org.talend.dataprep.qa.api;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.talend.dataprep.qa.api.config.APIStory;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.talend.dataprep.qa.api");

        Map<String, APIStory> stories = context.getBeansOfType(APIStory.class);

        for (APIStory story : stories.values()) {
            try {
                story.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

    }
}
