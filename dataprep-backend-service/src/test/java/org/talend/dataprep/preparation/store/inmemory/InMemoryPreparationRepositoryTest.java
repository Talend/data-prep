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

package org.talend.dataprep.preparation.store.inmemory;

import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.store.PersistentPreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepositoryTest;

/**
 * Unit test for the InMemoryPreparationRepository.
 *
 * @see InMemoryPreparationRepository
 */
public class InMemoryPreparationRepositoryTest extends PreparationRepositoryTest {

    /** The preparation repository to test. */
    private PreparationRepository repository;

    @Autowired
    private BeanConversionService beanConversionService;

    /**
     *
     */
    @Before
    public void before() {
        repository = new PersistentPreparationRepository(new InMemoryPreparationRepository(), beanConversionService);
        repository.clear();
    }

    /**
     * Test the getByDataSet method.
     */
    @Test
    public void getByDataSetTest() {

        // populate repository with noise (steps and preparations to be ignored)
        repository.add(getStep("s1"));
        repository.add(getStep("s2"));
        repository.add(getStep("s3"));
        repository.add(getStep("s4"));
        repository.add(getStep("s5"));

        repository.add(getPreparation("p1"));
        repository.add(getPreparation("p2"));
        repository.add(getPreparation("p3"));
        repository.add(getPreparation("p4"));
        repository.add(getPreparation("p5"));

        // add relevant data
        String dataSetId = "wantedId";
        Collection<Preparation> expected = Arrays.asList(getPreparation(dataSetId, "10"), getPreparation(dataSetId, "11"),
                getPreparation(dataSetId, "12"));
        for (Preparation preparation : expected) {
            repository.add(preparation);
        }

        // run the test
        Collection<Preparation> actual = getRepository().list(Preparation.class, eq("dataSetId", dataSetId)).collect(Collectors.toList());

        // check the result
        Assert.assertEquals(3, actual.size());
        for (Preparation preparation : expected) {
            Assert.assertTrue(actual.contains(preparation));
        }
    }

    /**
     * Helper method that only generates a step but simplify code.
     */
    private Step getStep(String rootName) {
        return new Step(rootName + "_parent", rootName + "_content", "1.0.PE");
    }

    /**
     * Helper method that only generates a preparation but simplify code.
     *
     * @param rootName root name for all the preparation attributes.
     * @return a new Preparation.
     */
    @Override
    protected Preparation getPreparation(String rootName) {
        final Preparation preparation = getPreparation(rootName + "_setId", rootName);
        preparation.setAuthor(rootName + "_setId");
        return preparation;
    }

    /**
     * Helper method that only generates a preparation but simplify code.
     *
     * @param datasetId the preparation dataset id.
     * @param rootName root name for all the preparation attributes.
     * @return a new Preparation.
     */
    private Preparation getPreparation(String datasetId, String rootName) {

        Preparation preparation = new Preparation(UUID.randomUUID().toString(), datasetId, getStep(rootName).id(), "1.0");
        preparation.setName(rootName + "_name");
        preparation.setAuthor(rootName + "_name");
        return preparation;
    }


    @Override
    protected PreparationRepository getRepository() {
        return repository;
    }
}
