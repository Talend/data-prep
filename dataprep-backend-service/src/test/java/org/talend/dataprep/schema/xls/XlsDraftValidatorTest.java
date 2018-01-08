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

package org.talend.dataprep.schema.xls;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.DraftValidator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Unit test for the XlsDraftValidator.
 * @see XlsDraftValidatorTest
 */
public class XlsDraftValidatorTest extends AbstractSchemaTestUtils {

    @Autowired
    private XlsDraftValidator validator;

    @Test
    public void shouldValidateDraft() throws Exception {
        // given
        final DataSetMetadata metadata = metadataBuilder.metadata().id("#123").sheetName("toto").build();
        // when
        final DraftValidator.Result result = validator.validate(metadata);
        // then
        assertThat(result.isDraft(), is(false));
    }

    @Test
    public void shouldNotValidateDraft() throws Exception {
        // given
        final DataSetMetadata metadata = metadataBuilder.metadata().id("#456").sheetName("").build();
        // when
        final DraftValidator.Result result = validator.validate(metadata);
        // then
        assertThat(result.isDraft(), is(true));
    }
}