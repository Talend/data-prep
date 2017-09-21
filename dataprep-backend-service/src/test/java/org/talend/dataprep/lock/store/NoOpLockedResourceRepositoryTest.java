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

package org.talend.dataprep.lock.store;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(MockitoJUnitRunner.class)
public class NoOpLockedResourceRepositoryTest {

    @InjectMocks
    private NoOpLockedResourceRepository noOpLockedResourceRepository;

    @Mock
    private PreparationRepository preparationRepository;

    @Test
    public void tryLock() throws Exception {

        // TODO

    }

    @Test
    public void unlock() throws Exception {

        // TODO

    }

}
