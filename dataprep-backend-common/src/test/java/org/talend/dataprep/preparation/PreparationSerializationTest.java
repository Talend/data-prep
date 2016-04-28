//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.preparation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PreparationTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public class PreparationSerializationTest {

    @Autowired
    Jackson2ObjectMapperBuilder builder;

    @Autowired
    PreparationRepository repository;

    @Autowired
    private VersionService versionService;

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;

    /** The default root content. */
    @Resource(name = "rootContent")
    private PreparationActions rootContent;

    @Test
    public void emptyPreparation() throws Exception {
        Preparation preparation = new Preparation("534fceed35b633160f2e2469f7ac7c14d75177b7", versionService.version().getVersionId());
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, preparation);
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("emptyPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void namePreparation() throws Exception {
        Preparation preparation = new Preparation("534fceed35b633160f2e2469f7ac7c14d75177b7", versionService.version().getVersionId());
        preparation.setName("MyName");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, preparation);
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("namePreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDataSet() throws Exception {
        Preparation preparation = new Preparation("b7368bd7e4de38ff954636d0ac0438c7fb56a208", versionService.version().getVersionId());
        preparation.setDataSetId("12345");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, preparation);
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("dataSetPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationAuthor() throws Exception {
        Preparation preparation = new Preparation("0c02c9f868217ecc9d619931e127268c68809e9e", versionService.version().getVersionId());
        preparation.setDataSetId("12345");
        preparation.setAuthor("myAuthor");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, preparation);
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("authorPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDetailsSteps() throws Exception {
        Preparation preparation = new Preparation("0c02c9f868217ecc9d619931e127268c68809e9e", "12345", rootStep.id(), versionService.version().getVersionId());
        preparation.setAuthor("myAuthor");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, new PreparationDetails(preparation));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("preparationDetailsSteps.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDetailsStepsWithActions() throws Exception {
        final String version = versionService.version().getVersionId();
        // Add a step
        final List<Action> actions = PreparationTest.getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = rootContent.append(actions);
        repository.add(newContent1);
        final Step s1 = new Step(rootStep.id(), newContent1.id(), version);
        repository.add(s1);
        // Use it in preparation
        Preparation preparation = new Preparation("b7368bd7e4de38ff954636d0ac0438c7fb56a208", "12345", s1.id(), version);
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        builder.build().writer().writeValue(output, new PreparationDetails(preparation));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("preparationDetailsWithStepsAndActions.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }
}
