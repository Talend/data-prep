// ============================================================================
//
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

package org.talend.dataprep.preparation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.audit.DataprepAuditService;

@RunWith(MockitoJUnitRunner.class)
public class PreparationServiceUnitTest {

    @Mock
    private DataprepAuditService auditService;

    @InjectMocks
    private PreparationService preparationService;

    @Test
    public void testCreateShouldLogAuditEventOnSuccess() {

    }

    @Test
    public void testCreateShouldNotLogAuditEventOnFailure() {

    }

}
