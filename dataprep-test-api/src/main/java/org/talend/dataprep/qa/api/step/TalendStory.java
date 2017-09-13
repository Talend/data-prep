package org.talend.dataprep.qa.api.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

public class TalendStory extends StoryContext {

    @Autowired
    protected ConfigurableEnvironment environment;

//    public RequestSpecification given() {
//        return io.restassured.RestAssured.given();
//    }


}
