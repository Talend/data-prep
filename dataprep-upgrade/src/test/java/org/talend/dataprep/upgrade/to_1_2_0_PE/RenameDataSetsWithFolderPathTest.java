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

package org.talend.dataprep.upgrade.to_1_2_0_PE;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Unit test for the org.talend.dataprep.upgrade.to_One_Two_Zero_PE.RenameDataSetsWithFolderPath class.
 *
 * @see RenameDataSetsWithFolderPath
 */

public class RenameDataSetsWithFolderPathTest extends Base_1_2_0_PE_Test {

    /** The task to test. */
    @Autowired
    private RenameDataSetsWithFolderPath task;

    /** The dataset metadata repository. */
    @Autowired
    private DataSetMetadataRepository repository;

    @Test
    public void shouldRenameDataSets() throws Exception {

        // given
        List<String> expected = new ArrayList<>();
        expected.add("cars");
        expected.add("us_states - my lookup");
        expected.add("communes_france - personnal/confidential");
        expected.add("Emails Reference - Quick Examples and Tutorials/Lookup");
        expected.add("Customer Contact Data - Quick Examples and Tutorials");
        expected.add("Business Unit Regions With States - Quick Examples and Tutorials/Lookup");
        expected.add("HRMS Export - Quick Examples and Tutorials");
        expected.add("nba_franchises");
        expected.add("Marketing Leads - Quick Examples and Tutorials");
        expected.add("CRM Export - Quick Examples and Tutorials");
        expected.add("Customer Marketing Leads - Quick Examples and Tutorials");

        // when
        task.run();

        // then
        final Stream<DataSetMetadata> list = repository.list();
        final List<String> names = list.map(DataSetMetadata::getName).collect(toList());
        assertTrue(names.containsAll(expected));
    }

    /**
     * @see Base_1_2_0_PE_Test#getTaskId()
     */
    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    /**
     * @see Base_1_2_0_PE_Test#getExpectedTaskOrder()
     */
    @Override
    protected int getExpectedTaskOrder() {
        return 0;
    }

}