package org.talend.dataprep.qa.step;

import cucumber.api.java.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.DataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;
import org.talend.dataprep.qa.bean.FeatureContext;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    @Autowired
    protected FeatureContext context;
    @Autowired
    protected DataPrepAPIHelper dpah;

    @After
    public void cleanUp() {
        // cleaning dataset
        context.getDatasetIds().forEach(datasetId -> dpah.deleteDataSet(datasetId).then().statusCode(200));
        context.clearDataset();

        // cleaning preparation
        context.getPreparationIds().forEach(preparationId -> dpah.deletePreparation(preparationId).then().statusCode(200));
        context.clearPreparation();
    }

}
