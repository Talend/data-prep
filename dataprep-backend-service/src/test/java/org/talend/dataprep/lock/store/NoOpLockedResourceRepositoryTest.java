/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.lock.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(MockitoJUnitRunner.class)
public class NoOpLockedResourceRepositoryTest {

    @InjectMocks
    private NoOpLockedResourceRepository noOpLockedResourceRepository;

    @Mock
    private PreparationRepository preparationRepository;

    @Test
    public void tryLock() throws Exception {
        // given
        String preparationId = "preparation id";
        Preparation preparationMock = mock(Preparation.class);
        when(preparationRepository.get(preparationId, Preparation.class)).thenReturn(preparationMock);

        // when
        Preparation preparation = noOpLockedResourceRepository.tryLock(preparationId, "Toto", "toto de Charleville-Mézières");

        // then
        assertEquals(preparationMock,preparation);
        verify(preparationRepository).get(preparationId, Preparation.class);
    }

    @Test
    public void tryLock_nullPrepThenException() throws Exception {
        // given
        String preparationId = "preparation id";
        when(preparationRepository.get(preparationId, Preparation.class)).thenReturn(null);

        // when
        try{
            noOpLockedResourceRepository.tryLock(preparationId, "toto", "Toto de Charleville-Mézières");
            fail();
        } catch (TDPException e) {

            // then
            assertEquals(e.getCode(), PREPARATION_DOES_NOT_EXIST);
        }
        verify(preparationRepository).get(preparationId, Preparation.class);
    }

}
