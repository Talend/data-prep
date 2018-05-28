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

package org.talend.dataprep.upgrade.to_2_4_0_PE;

import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.upgrade.StepRowMetadataCompute;

/**
 * Set the RowMetadata for every step <b>after</b> action parameter migration.
 */
@Component
public class RecomputeStepRowMetadata extends BaseUpgradeTaskTo_2_4_0_PE {

    @Autowired
    private StepRowMetadataCompute compute;

    @Override
    public void run() {
        compute.run();
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }
}
