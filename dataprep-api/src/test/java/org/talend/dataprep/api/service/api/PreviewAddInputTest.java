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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.talend.dataprep.api.preparation.Action;

/**
 * Unit test for the PreviewAddParameters class.
 * 
 * @see PreviewAddParameters
 */
public class PreviewAddInputTest {

    /** The validator factory. */
    private LocalValidatorFactoryBean validator;

    /**
     * Default constructor that initialize the validator factory.
     */
    public PreviewAddInputTest() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Test
    public void noDataSetIdNorPreparationId() {
        // given parameters without preparation not dataset id
        PreviewAddParameters input = new PreviewAddParameters();

        // when
        Set<ConstraintViolation<PreviewAddParameters>> constraintViolations = validator.validate(input);

        // then 3 violations (dataset & preparation id are missing, action is null and tdpIds is empty)
        Assert.assertEquals(3, constraintViolations.size());
    }

    @Test
    public void noDataSetId() {
        // given parameters without preparation not dataset id
        PreviewAddParameters input = new PreviewAddParameters();
        input.setPreparationId("prep#98435");

        // when
        Set<ConstraintViolation<PreviewAddParameters>> constraintViolations = validator.validate(input);

        // then 2 violations (action is null and tdpIds is empty)
        Assert.assertEquals(2, constraintViolations.size());
    }

    @Test
    public void noPreparationId() {
        // given parameters without preparation not dataset id
        PreviewAddParameters params = new PreviewAddParameters();
        params.setDatasetId("dataset#19843");

        // when
        Set<ConstraintViolation<PreviewAddParameters>> constraintViolations = validator.validate(params);

        // then 2 violations (action is null and tdpIds is empty)
        Assert.assertEquals(2, constraintViolations.size());
    }

    @Test
    public void validInput() {
        // given parameters without preparation not dataset id
        PreviewAddParameters input = new PreviewAddParameters();
        input.setDatasetId("dataset#19843");
        input.setActions(Collections.singletonList(new Action()));
        input.setTdpIds(Arrays.asList(1, 3));

        // when
        Set<ConstraintViolation<PreviewAddParameters>> constraintViolations = validator.validate(input);

        // then no violation
        Assert.assertTrue(constraintViolations.isEmpty());
    }

}