package org.talend.dataprep.qa.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.DataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;

/**
 * This unit test is here to check that all bean is injected correctly
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class SpringContextTest {

    @Autowired
    private DataPrepAPIHelper dpah;

    @Test
    public void testInjection() {
        Assert.assertNotNull(dpah);
    }
}
