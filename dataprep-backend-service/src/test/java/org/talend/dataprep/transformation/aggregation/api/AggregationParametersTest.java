//  ============================================================================
//
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

package org.talend.dataprep.transformation.aggregation.api;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Unit test for aggregation parameter validation.
 */

public class AggregationParametersTest {

    /** The validator factory. */
    private LocalValidatorFactoryBean validator;

    /**
     * Default constructor that initialize the validator factory.
     */
    public AggregationParametersTest() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Test
    public void invalidParametersNoDataSetIdNorPreparationId() {
        // given parameters without preparation not dataset id
        AggregationParameters parameters = new AggregationParameters();

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 3 violations (dataset & preparation id are missing, and operations and groupBy are empty)
        Assert.assertEquals(3, constraintViolations.size());
    }

    @Test
    public void invalidParametersPreparationIdNoOperations() {
        // given parameters with a preparation preparation id
        AggregationParameters parameters = new AggregationParameters();
        parameters.setPreparationId("prep#1234");

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 2 violations (operations and groupBy are empty)
        Assert.assertEquals(2, constraintViolations.size());
    }

    @Test
    public void invalidParametersDatasetIdNoOperations() {
        // given parameters with a dataset id
        AggregationParameters parameters = new AggregationParameters();
        parameters.setDatasetId("dataset#7568");

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 2 violations (operations and groupBy are empty)
        Assert.assertEquals(2, constraintViolations.size());
    }

    @Test
    public void invalidParametersGroupByEmpty() {
        // given parameters with a dataset id
        AggregationParameters parameters = new AggregationParameters();
        parameters.setDatasetId("dataset#7568");
        parameters.addGroupBy("0002");

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 1 violations (operations is empty)
        Assert.assertEquals(1, constraintViolations.size());
    }

    @Test
    public void invalidParametersOperationsEmpty() {
        // given parameters with a dataset id
        AggregationParameters parameters = new AggregationParameters();
        parameters.setDatasetId("dataset#7568");
        parameters.addOperation(new AggregationOperation("0002", Operator.SUM));

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 1 violations (groupBy is empty)
        Assert.assertEquals(1, constraintViolations.size());
    }

    @Test
    public void validParameters() {
        // given parameters with a dataset id
        AggregationParameters parameters = new AggregationParameters();
        parameters.setDatasetId("dataset#7568");
        parameters.addOperation(new AggregationOperation("0002", Operator.SUM));
        parameters.addGroupBy("0001");

        // when
        Set<ConstraintViolation<AggregationParameters>> constraintViolations = validator.validate(parameters);

        // then 1 violations (groupBy is empty)
        Assert.assertTrue(constraintViolations.isEmpty());
    }
}