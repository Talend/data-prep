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

package org.talend.dataprep.upgrade.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for the org.talend.dataprep.upgrade.model.UpgradeTaskId class.
 *
 * @see UpgradeTaskId
 */
public class UpgradeTaskIdTest {

    @Test
    public void shouldEquals() throws Exception {
        UpgradeTaskId id1 = new UpgradeTaskId("1.2.0", "name", 1);
        UpgradeTaskId id2 = new UpgradeTaskId("1.2.0", "name", 1);
        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void shouldCompareOrder() throws Exception {
        UpgradeTaskId id1 = new UpgradeTaskId("1.2.0", "name", 1);
        UpgradeTaskId id2 = new UpgradeTaskId("1.2.0", "name", 2);
        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    public void shouldCompareVersion() throws Exception {
        UpgradeTaskId id1 = new UpgradeTaskId("10.5.0", "name", 1);
        UpgradeTaskId id2 = new UpgradeTaskId("1.3.0", "name", 1);
        assertTrue(id1.compareTo(id2) > 0);
    }

    @Test
    public void shouldDealWithEdition() throws Exception {
        UpgradeTaskId id1 = new UpgradeTaskId("5.3.0-PE", "name", 1);
        UpgradeTaskId id2 = new UpgradeTaskId("5.3.1-EE", "name", 1);
        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    public void shouldDealWithShorterVersion() throws Exception {
        UpgradeTaskId id1 = new UpgradeTaskId("1.6.3", "name", 1);
        UpgradeTaskId id2 = new UpgradeTaskId("1.6", "name", 1);
        assertTrue(id1.compareTo(id2) > 0);
    }

}