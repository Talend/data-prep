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

package org.talend.dataprep.configuration;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.preparation.LocatePreparation;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.security.NoOpSecurityProxy;
import org.talend.dataprep.security.SecurityProxy;

import com.netflix.hystrix.HystrixCommand;

/**
 * Unit test for the APIPreparationConversions class.
 *
 * @see APIPreparationConversions
 */
@RunWith(MockitoJUnitRunner.class)
public class APIPreparationConversionsTest {

    @InjectMocks
    private APIPreparationConversions eeApiPreparationConversions;

    @Mock
    private ApplicationContext applicationContext;

    private BeanConversionService conversionService = new BeanConversionService();

    private boolean setup = false;

    @Before
    public void setUp() throws Exception {
        if (!setup) {
            conversionService = eeApiPreparationConversions.doWith(conversionService, "whateverTheName", applicationContext);
            when(applicationContext.getBean(eq(SecurityProxy.class))).thenAnswer(i -> new NoOpSecurityProxy());

            setup = true;
        }
    }

    @Test
    public void shouldEnrichPreparationWithDataset() {
        // given
        DataSetMetadata metadata = getDataSetMetadata("super dataset", 1001L);
        setupHystrixCommand(DataSetGetMetadata.class, metadata);

        final PreparationMessage preparation = getPreparationMessage(metadata.getId());

        Folder folder = getFolder("F-753854");
        setupHystrixCommand(LocatePreparation.class, folder);

        // when
        final EnrichedPreparation actual = conversionService.convert(preparation, EnrichedPreparation.class);

        // then
        assertEquals(metadata.getId(), actual.getSummary().getDataSetId());
        assertEquals(metadata.getName(), actual.getSummary().getDataSetName());
        assertEquals(metadata.getContent().getNbRecords(), actual.getSummary().getDataSetNbRow());

        final List<String> expectedSteps = preparation.getSteps().stream().map(Step::getId).collect(Collectors.toList());
        final List<String> actualSteps = actual.getSteps();
        assertNotNull(actualSteps);
        assertEquals(expectedSteps.size(), expectedSteps.size());
        expectedSteps.forEach(s -> assertTrue(actualSteps.contains(s)));

        assertEquals(folder, actual.getFolder());
    }

    @Test
    public void shouldNotEnrichPreparationWithDataset() {
        // given
        final PreparationMessage preparation = getPreparationMessage(null);

        // when
        final EnrichedPreparation actual = conversionService.convert(preparation, EnrichedPreparation.class);

        // then
        assertNotNull(actual);
        assertNull(actual.getSummary());
        assertNull(actual.getFolder());
    }

    @Test
    public void shouldDealWithExceptionInDataSetGet() {
        // given
        final PreparationMessage preparation = getPreparationMessage("DS-1234");

        when(applicationContext.getBean(eq(DataSetGet.class), any(Object[].class))).thenAnswer(i -> {
            final DataSetGet mock = mock(DataSetGet.class);
            when(mock.execute()).thenThrow(new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST));
            return mock;
        });

        // when
        final EnrichedPreparation actual = conversionService.convert(preparation, EnrichedPreparation.class);

        // then
        assertNotNull(actual);
        assertNull(actual.getSummary());
        assertNull(actual.getFolder());
    }

    @Test
    public void shouldDealWithRepeatedStepIds() {
        // given
        DataSetMetadata metadata = getDataSetMetadata("super dataset", 1001L);
        setupHystrixCommand(DataSetGetMetadata.class, metadata);

        final PreparationMessage preparation = getPreparationMessage(metadata.getId());
        preparation.setSteps(asList(Step.ROOT_STEP, Step.ROOT_STEP));

        Folder folder = getFolder("F-753854");
        setupHystrixCommand(LocatePreparation.class, folder);

        // when
        final EnrichedPreparation actual = conversionService.convert(preparation, EnrichedPreparation.class);

        // then
        assertEquals(1, actual.getSteps().size());
        assertEquals(Step.ROOT_STEP.id(), actual.getSteps().get(0));
    }


    private Folder getFolder(String name) {
        final Folder folder = new Folder();
        folder.setId(UUID.randomUUID().toString());
        folder.setName(name);
        return folder;
    }

    private DataSetMetadata getDataSetMetadata(String name, long nbRecords) {
        final DataSetMetadata metadata = new DataSetMetadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setName(name);
        metadata.getContent().setNbRecords(nbRecords);
        return metadata;
    }

    private PreparationMessage getPreparationMessage(String dataSetId) {
        final PreparationMessage preparation = new PreparationMessage();
        preparation.setDataSetId(dataSetId);
        List<Step> steps = new ArrayList<>(12);
        Step parentStep = preparation.getSteps().get(0);
        for (int i = 0; i < 12; i++) {
            final Step step = new Step(parentStep.id(), new PreparationActions().id(), "2.1");
            steps.add(step);
            parentStep = step;
        }
        preparation.setSteps(steps);
        return preparation;
    }

    private <T> void setupHystrixCommand(Class<? extends HystrixCommand<T>> commandClass, T result) {
        when(applicationContext.getBean(eq(commandClass), any(Object[].class))).thenAnswer(i -> {
            final HystrixCommand<T> mock = mock(commandClass);
            when(mock.execute()).thenReturn(result);
            return mock;
        });
    }

}
