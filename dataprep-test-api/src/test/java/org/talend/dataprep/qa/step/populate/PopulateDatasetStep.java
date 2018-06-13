//  ============================================================================
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.qa.step.populate;

import static org.talend.dataprep.qa.config.FeatureContext.removeSuffixName;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.step.ActionStep;
import org.talend.dataprep.qa.step.DatasetStep;
import org.talend.dataprep.qa.step.PreparationStep;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

/**
 * Step dealing with dataset.
 */
public class PopulateDatasetStep extends DataPrepStep {

    @Autowired
    private DatasetStep datasetStep;

    @Autowired
    private PreparationStep preparationStep;

    @Autowired
    private ActionStep actionStep;

    @Given("^I upload \"(.*)\" times the dataset \"(.*)\" with name \"(.*)\"$") //
    public void givenIUploadTheDataSetNTime(Integer nbTime, String fileName, String name) throws IOException {
        for (int i = 0; i < nbTime; i++) {
            String finalDatasetName = i + "_" + name;
            datasetStep.givenIUploadTheDataSet(fileName, finalDatasetName);
        }
    }

    @Given("^I create \"(.*)\" preparation \"(.*)\" with random dataset and \"(.*)\" steps with parameters:$")
    public void givenICreatePrepWithNStepNTime(Integer nbTime, String preparationName, Integer nbStep,
            DataTable dataTable) throws IOException {
        for (int i = 0; i < nbTime; i++) {
            String datasetName = removeSuffixName(getRandomDatasetName());
            String finalPrepName = i + "_" + preparationName;
            preparationStep.givenICreateAPreparation(finalPrepName, datasetName);
            for (int j = 0; j < (nbStep / 2); j++) {
                iCreateMultipleStepForPrep(finalPrepName, dataTable);
            }
        }
    }

    private void iCreateMultipleStepForPrep(String finalPrepName, DataTable dataTable) {
        actionStep.whenIAddAStepToAPreparation("uppercase", finalPrepName, dataTable);
        actionStep.whenIAddAStepToAPreparation("lowercase", finalPrepName, dataTable);
    }

    private String getRandomDatasetName() {
        List<String> listDataset = context.getDatasetNames();
        Random rand = new Random();
        return listDataset.get(rand.nextInt(listDataset.size()));
    }

}
