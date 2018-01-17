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

package org.talend.dataprep.api.service.api;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Unit test for the DynamicParamsInput class.
 * 
 * @see DynamicParamsInput
 */
public class DynamicParamsInputTest {

    /** The validator factory. */
    private LocalValidatorFactoryBean validator;

    /**
     * Default constructor that initialize the validator factory.
     */
    public DynamicParamsInputTest() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Test
    public void noDataSetIdNorPreparationId() {
        // given parameters without preparation not dataset id
        DynamicParamsInput input = new DynamicParamsInput();

        // when
        Set<ConstraintViolation<DynamicParamsInput>> constraintViolations = validator.validate(input);

        // then 2 violations (dataset & preparation id are missing & column id is empty)
        Assert.assertEquals(2, constraintViolations.size());
    }

    @Test
    public void noDataSetId() {
        // given parameters without preparation not dataset id
        DynamicParamsInput input = new DynamicParamsInput();
        input.setPreparationId("prep#98435");

        // when
        Set<ConstraintViolation<DynamicParamsInput>> constraintViolations = validator.validate(input);

        // then 1 violation (column id is empty)
        Assert.assertEquals(1, constraintViolations.size());
    }

    @Test
    public void noPreparationId() {
        // given parameters without preparation not dataset id
        DynamicParamsInput input = new DynamicParamsInput();
        input.setDatasetId("dataset#19843");

        // when
        Set<ConstraintViolation<DynamicParamsInput>> constraintViolations = validator.validate(input);

        // then 1 violation (column id is empty)
        Assert.assertEquals(1, constraintViolations.size());
    }

    @Test
    public void validInput() {
        // given parameters without preparation not dataset id
        DynamicParamsInput input = new DynamicParamsInput();
        input.setDatasetId("dataset#19843");
        input.setColumnId("7538");

        // when
        Set<ConstraintViolation<DynamicParamsInput>> constraintViolations = validator.validate(input);

        // then no violation
        Assert.assertTrue(constraintViolations.isEmpty());
    }
}