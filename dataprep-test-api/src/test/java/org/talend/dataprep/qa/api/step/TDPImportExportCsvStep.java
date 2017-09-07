package org.talend.dataprep.qa.api.step;

import cucumber.api.java.en.Given;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.helper.DataPrepAPIHelper;


public class TDPImportExportCsvStep {

    @Autowired
    private DataPrepAPIHelper dpah;

//    public void uploadDataset() throws java.io.IOException {
//        Given("I upload the dataset (.+) with name (.+)", (String filename, String name) -> {
//            Response response = dpah.uploadDataset(filename, name);
//        });

    @Given("I upload the dataset $filename with name $name")
    public void uploadDataset(String filename, String name) throws java.io.IOException {
        Response response = dpah.uploadDataset(filename, name);
        response.then().statusCode(200);
    }

}
