package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.helper.DataPrepAPIHelper;


public class DataSetStep implements En {

    @Autowired
    private DataPrepAPIHelper dpah;

    public DataSetStep() {
        Then("^The uploaded dataset is present in datasets list$", () -> {
            // Write code here that turns the phrase above into concrete actions
        });

    }
}
