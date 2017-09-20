package org.talend.dataprep.qa.step.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.DataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;
import org.talend.dataprep.qa.bean.FeatureContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    @Autowired
    protected FeatureContext context;

    @Autowired
    protected DataPrepAPIHelper dpah;

    protected ObjectMapper objectMapper = new ObjectMapper();
}
