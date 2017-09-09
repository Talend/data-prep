package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.helper.DataPrepAPIHelper;


public class TDPImportExportCsvStep implements En {

    @Autowired
    private DataPrepAPIHelper dpah;

    public TDPImportExportCsvStep() {
        When("^I upload the dataset (.*) with name (.*)$", (String fileName, String name) -> {
            System.out.println("fileName = " + fileName);
            System.out.println("name = " + name);
        });
    }

}
