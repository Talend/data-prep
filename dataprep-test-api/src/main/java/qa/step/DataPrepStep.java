package qa.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.DataPrepAPIHelper;
import qa.SpringContextConfiguration;
import qa.bean.FeatureContext;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    @Autowired
    protected FeatureContext context;
    @Autowired
    protected DataPrepAPIHelper dpah;

}
