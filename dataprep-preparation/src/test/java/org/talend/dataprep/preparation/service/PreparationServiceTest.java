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

package org.talend.dataprep.preparation.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.preparation.BasePreparationTest;

/**
 * Unit/integration tests for the PreparationService
 */
public class PreparationServiceTest extends BasePreparationTest {

    /** Where the folders are stored. */
    @Autowired
    private PreparationService preparationService;

    @Test
    public void should_list_all_preparations() throws Exception {
        // given and when
        init();

        // then : path should override other props
        assertThat(preparationService.listAll("dont_exist", "wrong_folder_path", "/foo/prep_name_foo", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : path should override other props
        assertThat(preparationService.listAll(null, null, "/foo/prep_name_foo", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : path should override other props
        assertThat(preparationService.listAll(null, null, "prep_name_home", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : path should override other props
        assertThat(preparationService.listAll(null, null, "/prep_name_home", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : path should override other props
        assertThat(preparationService.listAll("dont_exist", "wrong_folder_path", null, null, null) //
                .collect(Collectors.toList()).size(), is(0));

        // then : should be the normal behaviour without path parameter
        assertThat(preparationService.listAll("prep_name_home", "/", null, null, null) //
                .collect(Collectors.toList()).size(), is(1));
        assertThat(preparationService.listAll("prep_name_foo", "/foo", null, null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : : should list if path doesn't start with "/"
        assertThat(preparationService.listAll(null, null, "foo/prep_name_foo", null, null).collect(Collectors.toList()).size(),
                is(1));

        // then : should list if path starts with "/"
        assertThat(preparationService.listAll(null, null, "/foo/prep_name_foo", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : path doesn't contain "/"
        assertThat(preparationService.listAll(null, null, "prep_name_home", null, null) //
                .collect(Collectors.toList()).size(), is(1));

        // then : : should list preparation with special character in preparation (see
        // https://jira.talendforge.org/browse/TDP-4779)
        assertThat(preparationService
                .listAll(null, null, "foo/Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv", null, null)
                .collect(Collectors.toList()).size(), is(1));

        // then : : should list preparation with special character in folder and preparation (see
        // https://jira.talendforge.org/browse/TDP-4779)
        assertThat(preparationService.listAll(null, null,
                "Folder Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv/Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv",
                null, null).collect(Collectors.toList()).size(), is(1));
    }

    private void init() throws IOException {
        createFolder(home.getId(), "foo");
        final Folder foo = getFolder(home.getId(), "foo");

        createFolder(home.getId(), "Folder Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv");
        final Folder specialChar = getFolder(home.getId(), "Folder Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv");

        Preparation preparation = new Preparation();
        preparation.setName("prep_name_foo");
        preparation.setDataSetId("1234");
        preparation.setRowMetadata(new RowMetadata());
        clientTest.createPreparation(preparation, foo.getId());

        preparation.setName("prep_name_home");
        clientTest.createPreparation(preparation, home.getId());

        preparation.setName("Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv");
        clientTest.createPreparation(preparation, foo.getId());

        preparation.setName("Cr((eate E?mail A!dd\"ressrrrbb[zzzz (copie*-é'(-è_çà)+&.csv");
        clientTest.createPreparation(preparation, specialChar.getId());
    }
}
