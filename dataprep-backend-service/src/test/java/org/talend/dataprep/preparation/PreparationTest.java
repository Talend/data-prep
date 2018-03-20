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

package org.talend.dataprep.preparation;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class PreparationTest extends ServiceBaseTest {

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private VersionService versionService;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        repository.clear();
    }

    @Test
    public void testDefaultPreparation() throws Exception {

        final Preparation preparation = new Preparation("#123", "12345", Step.ROOT_STEP.id(), versionService.version().getVersionId());
        preparation.setCreationDate(0L);

        assertThat(preparation.id(), is("#123"));
        assertThat(preparation.getHeadId(), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void rootObjects() throws Exception {
        assertThat(repository.get(PreparationActions.ROOT_ACTIONS.getId(), PreparationActions.class), notNullValue());
        assertThat(repository.get(PreparationActions.ROOT_ACTIONS.getId(), PreparationActions.class).getId(),
                is("cdcd5c9a3a475f2298b5ee3f4258f8207ba10879"));
        assertThat(repository.get(PreparationActions.ROOT_ACTIONS.getId(), Step.class), nullValue());

        assertThat(repository.get(Step.ROOT_STEP.getId(), PreparationActions.class), nullValue());
        assertThat(repository.get(Step.ROOT_STEP.getId(), Step.class), notNullValue());
        assertThat(repository.get(Step.ROOT_STEP.getId(), Step.class).getId(), is("f6e172c33bdacbc69bca9d32b2bd78174712a171"));
    }

    @Test
    public void testTimestamp() throws Exception {
        Preparation preparation = new Preparation("#584284", "1234", Step.ROOT_STEP.id(), versionService.version().getVersionId());
        final long time0 = preparation.getLastModificationDate();
        TimeUnit.MILLISECONDS.sleep(50);
        preparation.updateLastModificationDate();
        final long time1 = preparation.getLastModificationDate();
        assertThat(time0, lessThan(time1));
    }

    @Test
    public void testId_withName() throws Exception {
        // Preparation id with name
        Preparation preparation = new Preparation("#87374", "1234", Step.ROOT_STEP.id(), versionService.version().getVersionId());
        preparation.setName("My Preparation");
        preparation.setCreationDate(0L);
        final String id0 = preparation.getId();
        assertThat(id0, is("#87374"));
        // Same preparation (but with empty name): id must remain same
        preparation.setName("");
        final String id1 = preparation.getId();
        assertThat(id1, is("#87374"));
        // Same preparation (but with null name, null and empty names should be treated all the same): id must remain same
        preparation.setName(null);
        final String id2 = preparation.getId();
        assertThat(id2, is("#87374"));
    }

    @Test
    public void initialStep() {
        final String version = versionService.version().getVersionId();

        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");

        final PreparationActions newContent = new PreparationActions(actions, version);
        repository.add(newContent);

        final Step s = new Step(Step.ROOT_STEP.id(), newContent.id(), version);
        repository.add(s);

        Preparation preparation = new Preparation("#48368", "1234", s.id(), version);
        preparation.setCreationDate(0L);
        repository.add(preparation);

        assertThat(preparation.id(), Is.is("#48368"));
    }

    @Test
    public void initialStepWithAppend() {
        final String version = versionService.version().getVersionId();
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");

        final PreparationActions newContent = PreparationActions.ROOT_ACTIONS.append(actions);
        repository.add(newContent);

        final Step s = new Step(Step.ROOT_STEP.id(), newContent.id(), version);
        repository.add(s);

        final Preparation preparation = new Preparation("#5438743", "1234", s.id(), version);
        preparation.setCreationDate(0L);
        repository.add(preparation);

        assertThat(preparation.id(), Is.is("#5438743"));
    }

    @Test
    public void stepsWithAppend() {
        final String version = versionService.version().getVersionId();
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");

        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        repository.add(newContent1);
        final PreparationActions newContent2 = newContent1.append(actions);
        repository.add(newContent2);

        // Steps
        final Step s1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        repository.add(s1);

        final Step s2 = new Step(s1.id(), newContent2.id(), version);
        repository.add(s2);

        // Preparation
        final Preparation preparation = new Preparation("#54258728", "1234", s2.id(), version);
        preparation.setCreationDate(0L);
        repository.add(preparation);

        assertThat(preparation.id(), Is.is("#54258728"));
    }

    @Test
    public void should_merge_from_other() {
        Preparation source = new Preparation("#4837", versionService.version().getVersionId());

        Preparation theOtherOne = new Preparation("#4837", versionService.version().getVersionId());
        theOtherOne.setAuthor("Joss Stone");
        theOtherOne.setCreationDate(source.getCreationDate() - 1000);
        theOtherOne.setDataSetId("ds#123456");
        theOtherOne.setLastModificationDate(theOtherOne.getCreationDate() + 12345682);
        theOtherOne.setName("my preparation name");
        theOtherOne.setHeadId(Step.ROOT_STEP.id());

        Preparation actual = source.merge(theOtherOne);

        assertEquals(actual, theOtherOne);
    }

    @Test
    public void should_merge_from_source() {
        Preparation theOtherOne = new Preparation("#23874", versionService.version().getVersionId());

        Preparation source = new Preparation("#158387", versionService.version().getVersionId());
        source.setAuthor("Bloc Party");
        source.setCreationDate(theOtherOne.getCreationDate() - 1000);
        source.setDataSetId("ds#65478");
        source.setLastModificationDate(source.getCreationDate() + 2658483);
        source.setName("banquet");
        source.setHeadId(Step.ROOT_STEP.id());

        Preparation actual = source.merge(theOtherOne);

        assertEquals(actual, source);
    }

    public static List<Action> getSimpleAction(final String actionName, final String paramKey, final String paramValue) {
        final Action action = new Action();
        action.setName(actionName);
        action.getParameters().put(paramKey, paramValue);

        final List<Action> actions = new ArrayList<>();
        actions.add(action);

        return actions;
    }
}
