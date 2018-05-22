// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.preparation;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PreparationSerializationTest extends ServiceBaseTest {

    @Autowired
    PreparationRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VersionService versionService;

    @Autowired
    private BeanConversionService conversionService;

    @Test
    public void emptyPreparation() throws Exception {
        Preparation preparation =
                new Preparation("534fceed35b633160f2e2469f7ac7c14d75177b7", versionService.version().getVersionId());
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        mapper.writeValue(output, conversionService.convert(preparation, PreparationMessage.class));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("emptyPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void namePreparation() throws Exception {
        Preparation preparation =
                new Preparation("534fceed35b633160f2e2469f7ac7c14d75177b7", versionService.version().getVersionId());
        preparation.setName("MyName");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(preparation, PreparationMessage.class));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("namePreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDataSet() throws Exception {
        Preparation preparation =
                new Preparation("b7368bd7e4de38ff954636d0ac0438c7fb56a208", versionService.version().getVersionId());
        preparation.setDataSetId("12345");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(preparation, PreparationMessage.class));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("dataSetPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationAuthor() throws Exception {
        Preparation preparation =
                new Preparation("0c02c9f868217ecc9d619931e127268c68809e9e", versionService.version().getVersionId());
        preparation.setDataSetId("12345");
        preparation.setAuthor("myAuthor");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(preparation, PreparationMessage.class));
        final InputStream expected = PreparationSerializationTest.class.getResourceAsStream("authorPreparation.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDetailsSteps() throws Exception {
        Preparation preparation = new Preparation("0c02c9f868217ecc9d619931e127268c68809e9e", "12345",
                Step.ROOT_STEP.id(), versionService.version().getVersionId());
        preparation.setAuthor("myAuthor");
        preparation.setCreationDate(0L);
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(preparation, PreparationMessage.class));
        final InputStream expected =
                PreparationSerializationTest.class.getResourceAsStream("preparationDetailsSteps.json");
        assertThat(output.toString(), sameJSONAsFile(expected));
    }

    @Test
    public void preparationDetailsStepsWithActions() throws Exception {

        final String version = versionService.version().getVersionId();
        // Add a step
        final List<Action> actions = PreparationTest.getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        repository.add(newContent1);
        final Step s1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        repository.add(s1);
        // Use it in preparation
        Preparation preparation =
                new Preparation("b7368bd7e4de38ff954636d0ac0438c7fb56a208", "12345", s1.id(), version);
        preparation.setCreationDate(0L);
        repository.add(preparation);

        final Preparation storedPreparation = repository.get(preparation.id(), Preparation.class);

        // when
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(storedPreparation, PreparationMessage.class));

        // then
        final PreparationMessage actual = mapper.readerFor(PreparationMessage.class).readValue(output.toString());
        assertEquals(preparation.getId(), actual.getId());
        assertNotNull(actual.getActions());
        assertEquals(1, actual.getActions().size());
        assertNotNull(actual.getSteps());
        assertEquals(2, actual.getSteps().size());

    }

    @Test
    public void preparationDetailsStepsWithFilters() throws Exception {

        final String version = versionService.version().getVersionId();
        // create row metadata
        RowMetadata rowMetadata = new RowMetadata();
        ColumnMetadata firstColumn = new ColumnMetadata();
        firstColumn.setId("0000");
        ColumnMetadata secondColumn = new ColumnMetadata();
        secondColumn.setId("0001");
        ColumnMetadata thirdColumn = new ColumnMetadata();
        thirdColumn.setId("0002");
        rowMetadata.setColumns(asList(firstColumn, secondColumn, thirdColumn));

        // Add a step
        final Action action1 = new Action();
        action1.setName("uppercase");
        action1.getParameters().put("column_name", "lastname");
        action1.getParameters().put("column_id", "0000");
        action1.getParameters().put(ImplicitParameters.FILTER.getKey(), "((0000 = 'charles'))");

        final List<Action> actions1 = new ArrayList<>();
        actions1.add(action1);

        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions1);
        repository.add(newContent1);
        final Step s1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);

        final String stepRowMetadataId = UUID.randomUUID().toString();
        s1.setRowMetadata(stepRowMetadataId);
        repository.add(s1);

        final Action action2 = new Action();
        action2.setName("uppercase");
        action2.getParameters().put("column_name", "firstname");
        action2.getParameters().put("column_id", "0001");
        action2.getParameters().put(ImplicitParameters.FILTER.getKey(), "((0001 = 'newen'))");

        final List<Action> actions2 = new ArrayList<>();
        actions2.add(action2);

        final PreparationActions newContent2 = newContent1.append(actions2);
        repository.add(newContent2);
        final Step s2 = new Step(s1.id(), newContent2.id(), version);
        s2.setRowMetadata(stepRowMetadataId);
        repository.add(s2);

        // Use it in preparation
        Preparation preparation =
                new Preparation("b7368bd7e4de38ff954636d0ac0438c7fb56a208", "12345", s2.id(), version);
        preparation.setCreationDate(0L);
        preparation.setRowMetadata(rowMetadata);

        repository.add(preparation);

        // add StepRowMetadata
        StepRowMetadata stepRowMetadata = new StepRowMetadata();
        stepRowMetadata.setId(stepRowMetadataId);
        stepRowMetadata.setRowMetadata(rowMetadata);
        repository.add(stepRowMetadata);

        final Preparation storedPreparation = repository.get(preparation.id(), Preparation.class);

        // when
        final StringWriter output = new StringWriter();
        mapper.writer().writeValue(output, conversionService.convert(storedPreparation, PreparationMessage.class));

        // then
        final PreparationMessage actual = mapper.readerFor(PreparationMessage.class).readValue(output.toString());
        assertEquals(preparation.getId(), actual.getId());
        assertNotNull(actual.getActions());
        assertEquals(2, actual.getActions().size());
        assertEquals(3, actual.getActions().get(0).getFilterColumns().size());
        assertEquals(1, actual.getActions().get(1).getFilterColumns().size());
        assertNotNull(actual.getSteps());
        assertEquals(3, actual.getSteps().size());

    }
}
