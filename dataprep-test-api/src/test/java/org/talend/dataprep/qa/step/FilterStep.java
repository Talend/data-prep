package org.talend.dataprep.qa.step;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.PreparationContent;

import java.io.IOException;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;

public class FilterStep extends DataPrepStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterStep.class);

    @When("^I apply the filter \"(.*)\" on dataset \"(.*)\"$")
    public void applyFilterOnDataSet(String tql, String datasetName) throws Exception {
        doApplyFilterOnDataSet(tql, datasetName);
    }

    private void doApplyFilterOnDataSet(String tql, String datasetName) throws Exception {
        DatasetContent datasetContent = getDatasetContent(context.getDatasetId(suffixName(datasetName)), tql);
        context.storeObject("dataSetContent", datasetContent);
    }

    @Then("^The step \"(.*)\" is applied with the filter \"(.*)\"$")
    public void theStepIsAppliedWithTheFilter(String step, String filter) {
        Action prepStep = context.getAction(step);
        Assert.assertEquals(filter, prepStep.parameters.get(FILTER.getKey()));
    }

    @When("^I remove all filters on dataset \"(.*)\"$")
    public void removeFilter(String datasetName) throws Exception {
        doApplyFilterOnDataSet(null, datasetName);
    }

    @When("^I apply the filter \"(.*)\" on the preparation \"(.*)\"$")
    public void applyFilterOnPreparation(String tql, String preparationName) throws Exception {
        doApplyFilterOnPreparation(tql, preparationName);
    }

    private void doApplyFilterOnPreparation(String tql, String preparationName) throws IOException {
        PreparationContent preparationContent = getPreparationContent(preparationName, tql);
        context.storeObject("preparationContent", preparationContent);
    }

    @Then("^I remove all filters on preparation \"(.*)\"$")
    public void removeAllFiltersOnPreparation(String preparationName) throws Exception {
        doApplyFilterOnPreparation(null, preparationName);
    }

}
