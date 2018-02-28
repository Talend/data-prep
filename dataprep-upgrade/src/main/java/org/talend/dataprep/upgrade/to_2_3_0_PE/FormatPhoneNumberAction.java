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

import org.springframework.stereotype.Component;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.common.ActionFormatPhoneNumber;

import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

@Component
public class FormatPhoneNumberAction extends BaseUpgradeTaskTo_2_3_0_PE {

    private final PreparationRepository preparationRepository;

    public FormatPhoneNumberAction(PreparationRepository preparationRepository) {
        this.preparationRepository = preparationRepository;
    }

    @Override public void run() {
        ActionFormatPhoneNumber.upgradeActions(preparationRepository);
    }

    @Override public int getOrder() {
        return 2;
    }

    @Override public target getTarget() {
        return VERSION;
    }
}
