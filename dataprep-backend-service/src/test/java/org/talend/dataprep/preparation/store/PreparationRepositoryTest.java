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

package org.talend.dataprep.preparation.store;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.preparation.Preparation;

public abstract class PreparationRepositoryTest extends ServiceBaseTest {

    protected abstract Preparation getPreparation(String preparationName);

    protected abstract PreparationRepository getRepository();

    @Test
    public void shouldListOnlyWantedClass() {

        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.forEach(prep -> getRepository().add(prep));

        // list all preparations
        final List<Preparation> actual = getRepository().list(Preparation.class).collect(Collectors.toList());

        assertEquals(ids.size(), actual.size());
        preparations.forEach(actual::contains);
    }

    @Test
    public void shouldListOnlyPreparationsForDatasets() {
        List<Integer> ids = Arrays.asList(1, 2, 3);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.forEach(prep -> getRepository().add(prep));

        // get preparation by name
        final Preparation expected = preparations.get(1);
        final Collection<Preparation> actual = getRepository().list(Preparation.class, eq("dataSetId", expected.getDataSetId())).collect(Collectors.toList());

        assertEquals(1, actual.size());
        assertTrue(actual.contains(expected));

    }

    @Test
    public void findOneByDataset_should_return_a_preparation_that_use_dataset() {
        // given
        final String dataSetId = "789b61f3128a9bc24a684";
        final Preparation prep1 = new Preparation();
        prep1.setDataSetId("other_dataset");
        final Preparation prep2 = new Preparation();
        prep2.setDataSetId(dataSetId);

        getRepository().add(prep1);
        getRepository().add(prep2);

        // when
        final boolean result = getRepository().exist(Preparation.class, eq("dataSetId", dataSetId));

        // then
        assertThat(result, is(true));
    }

    @Test
    public void findOneByDataset_should_return_null_when_no_preparation_use_dataset() {
        // given
        final String dataSetId = "789b61f3128a9bc24a684";
        final Preparation prep1 = new Preparation();
        prep1.setDataSetId("other_dataset");
        final Preparation prep2 = new Preparation();
        prep2.setDataSetId("other_dataset");

        getRepository().add(prep1);
        getRepository().add(prep2);

        // when
        final boolean result = getRepository().exist(Preparation.class, eq("dataSetId", dataSetId));

        // then
        assertThat(result, is(false));
    }

}
