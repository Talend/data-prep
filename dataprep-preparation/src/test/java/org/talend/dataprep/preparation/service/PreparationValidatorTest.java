/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.preparation.service;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.INVALID_PREPARATION;

import java.util.Set;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;

public class PreparationValidatorTest {

    @Test
    public void validateOrthrowException_correctPreparationIsValid() throws Exception {
        Preparation preparation = new Preparation();
        preparation.setRowMetadata(new RowMetadata());
        PreparationValidator.validateOrThrowException(preparation);
    }

    @Test
    public void validateOrthrowException_noRowMetadataThrowException() throws Exception {
        Preparation preparation = new Preparation();
        try {
            PreparationValidator.validateOrThrowException(preparation);
            fail();
        } catch (TDPException e) {
            assertThat(e.getCode(), is(INVALID_PREPARATION));
            assertThat(e.getContext().contains("message"), is(true));
        }
    }

    @Test(expected = TDPException.class)
    public void validateOrthrowException_nullPreparationThrowException() throws Exception {
        PreparationValidator.validateOrThrowException(null);
    }

    @Test
    public void validate_noRowMetadataIsInValid() throws Exception {
        Preparation preparation = new Preparation();
        Set<PreparationValidator.Violation> validation = PreparationValidator.validate(preparation);
        assertThat(validation, is(not(empty())));
        PreparationValidator.Violation first = validation.iterator().next();
        assertThat(first, not(nullValue()));
        assertThat(first.getMessage(), not(nullValue()));
        assertThat(first.getField(), is("rowMetadata"));

    }

}
