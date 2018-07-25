/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.user.store.UserDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserDataSetMetadataConversionTest {

    private static final String AUTHOR_ID = UUID.randomUUID().toString();

    private static final String DATASET_ID = UUID.randomUUID().toString();

    public static final String AUTHOR_NAME = "Foo";

    private BeanConversionService beanConversionService = new BeanConversionService();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Security security;

    @Mock
    private UserDataRepository userDataRepository;

    @Before
    public void setUp() {
        // conversion registration
        new UserDataSetMetadataConversion().doWith(beanConversionService, "userDataSetMetadataConversion",
                applicationContext);

        // mock user information
        when(applicationContext.getBean(Security.class)).thenReturn(security);
        when(security.getUserId()).thenReturn(AUTHOR_ID);
        when(security.getUserDisplayName()).thenReturn(AUTHOR_NAME);

        // add dataset as favorite
        when(applicationContext.getBean(UserDataRepository.class)).thenReturn(userDataRepository);
        UserData userData = new UserData();
        userData.addFavoriteDataset(DATASET_ID);
        when(userDataRepository.get(AUTHOR_ID)).thenReturn(userData);
    }

    @Test
    public void shouldConvertDataSetMetadataWithoutRowMetadata() {
        // given DataSetMetadata without rowMetadata
        RowMetadata rowMetadata = new RowMetadata(Collections.emptyList());
        DataSetMetadata dataSetMetadata = new DataSetMetadata(DATASET_ID, "name", AUTHOR_ID, 0L, 0L, rowMetadata, "1.0");
        // when
        UserDataSetMetadata userDataSetMetadata =
                beanConversionService.convert(dataSetMetadata, UserDataSetMetadata.class);
        // then
        assertNotNull(userDataSetMetadata);

        assertEquals(AUTHOR_ID, userDataSetMetadata.getOwnerId());
        Owner owner = new Owner(AUTHOR_ID, AUTHOR_NAME, "");
        assertEquals(owner, userDataSetMetadata.getOwner());

        assertTrue(userDataSetMetadata.isFavorite());
        assertTrue(userDataSetMetadata.getRoles().isEmpty());
    }
}