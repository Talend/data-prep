package org.talend.dataprep.qa.api.feature;

import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

public class TalendFeature {

    @Autowired
    protected ConfigurableEnvironment environment;

    public RequestSpecification given() {
        return io.restassured.RestAssured.given();
    }

}
