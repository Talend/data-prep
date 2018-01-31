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

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.USER;

import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * Preparations' id are no longer computed from a hash of some of their properties. Their id must be computed and set.
 */
@Component
public class ComputePreparationId implements BaseUpgradeTaskTo_1_2_0_PE {

    /** This class' logger. */
    private static final Logger LOG = getLogger(ComputePreparationId.class);

    @Autowired
    private PreparationRepository repository;

    /**
     * @see UpgradeTask#getOrder()
     */
    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public target getTarget() {
        return USER;
    }

    /**
     * @see UpgradeTask#run()
     */
    @Override
    public void run() {

        final Stream<Preparation> preparations = repository.list(Preparation.class);
        preparations.forEach(p -> {
            // preparation needs to be removed first as its id will change
            repository.remove(p);

            // change the preparation id and save id
            p.setId(UUID.randomUUID().toString());
            repository.add(p);

            LOG.debug("preparation {} has a new id {}", p.getName(), p.getName());
        });
    }

}
