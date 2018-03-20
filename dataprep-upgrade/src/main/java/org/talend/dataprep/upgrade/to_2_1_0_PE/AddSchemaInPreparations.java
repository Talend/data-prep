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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.store.file.FileSystemPreparationRepository;
import org.talend.dataprep.upgrade.common.PreparationDatasetRowUpdater;

@Component
public class AddSchemaInPreparations implements BaseUpgradeTaskTo_2_1_0_PE {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddSchemaInPreparations.class);

    @Autowired
    private PreparationDatasetRowUpdater preparationDatasetRowUpdater;

    @Autowired
    PreparationRepository preparationRepository;

    private FileSystemPreparationRepository fileSystemPreparationRepository;

    @PostConstruct
    private void postInitialize() {
        try {
            fileSystemPreparationRepository = (FileSystemPreparationRepository) FieldUtils.readField(preparationRepository,
                    "delegate", true);
        } catch (IllegalAccessException e) {
            LOGGER.error("Impossible to get access to the delegate preparation repository object");
        }
    }

    @Override
    public void run() {
        LOGGER.info("Starting to add row metadata to preparations.");
        preparationDatasetRowUpdater.updatePreparations(fileSystemPreparationRepository);
        LOGGER.info("Row metadata added to preparations.");
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }
}
