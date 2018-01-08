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

package org.talend.dataprep.maintenance;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.task.TaskExecutor;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.tenancy.ForAll;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseMaintenanceTest {

    @Mock
    protected ForAll forAll;

    @Mock
    protected SecurityProxy securityProxy;

    @Mock
    protected TaskExecutor executor;

    @Before
    public void setUp() throws Exception {
        when(forAll.condition()).thenReturn(bean -> () -> true);
        doAnswer(invocation -> {
            final Runnable runnable = (Runnable) invocation.getArguments()[1];
            runnable.run();
            return null;
        }).when(forAll).execute(any(), any());

        doAnswer(invocation -> {
            final Runnable runnable = (Runnable) invocation.getArguments()[0];
            runnable.run();
            return null;
        }).when(executor).execute(any());
    }
}
