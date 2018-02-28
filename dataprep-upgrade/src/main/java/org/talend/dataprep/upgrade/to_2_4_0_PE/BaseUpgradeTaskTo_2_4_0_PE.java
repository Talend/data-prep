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

package org.talend.dataprep.upgrade.to_2_4_0_PE;

import org.talend.dataprep.upgrade.model.UpgradeTask;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Base class for the 2.4.0-EE upgrade.
 */
public abstract class BaseUpgradeTaskTo_2_4_0_PE implements UpgradeTask {

    @Override
    public UpgradeTaskId getId() {
        return getTaskId("2.4.0-PE", this.getClass().getSimpleName());
    }

}
