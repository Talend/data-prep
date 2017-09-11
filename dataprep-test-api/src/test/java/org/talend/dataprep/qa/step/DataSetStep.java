package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.helper.DataPrepAPIHelper;


/**
 * Step dealing with dataset
 */
public class DataSetStep implements En {

    @Autowired
    private DataPrepAPIHelper dpah;

    /**
     * Default constructor
     */
    public DataSetStep() {

        Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$", (String fileName, String name) -> {
            System.out.println("fileName = " + fileName);
            System.out.println("name = " + name);
        });

    }
}
