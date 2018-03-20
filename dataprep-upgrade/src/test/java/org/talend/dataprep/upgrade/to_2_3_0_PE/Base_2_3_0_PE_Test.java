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

package org.talend.dataprep.upgrade.to_2_3_0_PE;

import org.junit.BeforeClass;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.upgrade.BasePEUpgradeTest;

/**
 * Base class for all 2.3.0 PE tests.
 */
@TestPropertySource(locations = { "to_2_3_0_PE.properties" })
public abstract class Base_2_3_0_PE_Test extends BasePEUpgradeTest {

    @BeforeClass
    public static void baseSetUp() throws Exception {
        setupStore("2.1.1-PE");
    }

    @Override
    protected String getExpectedVersion() {
        return "2.3.0-PE";
    }

}
