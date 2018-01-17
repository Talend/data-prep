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

package org.talend.dataprep.user.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.user.UserData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all UserDataRepository implementations.
 */
public abstract class AbstractUserDataRepositoryTest<U extends UserData> extends ServiceBaseTest {

    @Autowired
    private ObjectMapper mapper;

    protected abstract UserDataRepository<U> getUserRepository();

    /**
     * @return a random user data.
     */
    protected abstract U getRandomUserData();

    @Before
    public void cleanBefore() {
        getUserRepository().clear();
    }


    @Test
    public void shouldGetWhatWasAdded() throws JsonProcessingException {

        U expected = getRandomUserData();
        expected.addFavoriteDataset("dataset#987654");
        expected.addFavoriteDataset("dataset#lkj-sfdgs-63563-sfgsfg'");


        getUserRepository().save(expected);

        final U actual = getUserRepository().get(expected.getUserId());
        assertEquals(expected, actual);
    }

    @Test
    public void saveTwiceShouldUpdateUserData() {

        U expected = getRandomUserData();
        expected.addFavoriteDataset("dataset#987654");
        expected.addFavoriteDataset("dataset#lkj-sfdgs-63563-sfgsfg'");

        getUserRepository().save(expected);

        final Set<String> favorites = expected.getFavoritesDatasets();
        favorites.clear();
        favorites.add("dataset#kljkdflkdjqsfhqlkjsdfhqslkj");
        favorites.add("dataset#kiuyftuozyfgtzeuifytzeaiufyt");

        getUserRepository().save(expected);

        final U actual = getUserRepository().get(expected.getUserId());
        assertEquals(expected, actual);
    }

    @Test
    public void saveNullShouldNotThrowException() {
        getUserRepository().save(null);
    }

    @Test
    public void removeShouldRemoveFile() {
        U userData = getRandomUserData();
        userData.addFavoriteDataset("dataset#1");
        userData.addFavoriteDataset("dataset#2");

        getUserRepository().save(userData);
        getUserRepository().remove(userData.getUserId());

        assertNull(getUserRepository().get(userData.getUserId()));
    }

    @Test
    public void clearShouldRemoveAllFiles() {

        int count = 24;
        for (int i = 0; i < count; i++) {
            U userData = getRandomUserData();
            userData.addFavoriteDataset("dataset#" + i);
            userData.addFavoriteDataset("dataset#" + i + 1);
            getUserRepository().save(userData);
        }

        getUserRepository().clear();
        for (int i = 0; i < count; i++) {
            assertNull(getUserRepository().get(String.valueOf(i)));
        }

    }

}
