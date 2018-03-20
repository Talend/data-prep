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
package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;


/**
 * Fix the list of steps in PersistentPreparation.
 */
@Component
public class StepListPreparationsMigration implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(StepRowMetadataMigration.class);

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private PreparationUtils preparationUtils;

    @Override
    public void run() {
        LOGGER.info("Migration of step ids in preparation...");

        preparationRepository.list(PersistentPreparation.class) //
                .forEach(p -> {
                    LOGGER.info("Migration of preparation #{}", p.getId());
                    final List<String> stepsIds = preparationUtils.listStepsIds(p.getHeadId(), preparationRepository);
                    p.setSteps(stepsIds);

                    preparationRepository.add(p);
                    LOGGER.info("Migration of preparation #{} done ({} steps)", p.getId(), stepsIds.size());
                });
        LOGGER.info("Migration of step ids in preparation done.");
    }

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public UpgradeTask.target getTarget() {
        return VERSION;
    }
}
