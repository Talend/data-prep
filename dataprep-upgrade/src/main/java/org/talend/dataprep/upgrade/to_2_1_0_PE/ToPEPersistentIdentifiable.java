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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.store.file.FileSystemPreparationRepository;

@Component
public class ToPEPersistentIdentifiable implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ToPEPersistentIdentifiable.class);

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

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
        LOGGER.debug("starting upgrade from {} to {}.", Step.class, PersistentStep.class);
        final AtomicLong counter = new AtomicLong(0L);
        fileSystemPreparationRepository.list(Step.class).forEach(s -> {
            fileSystemPreparationRepository.remove(s);
            PersistentStep persistentStep = turnToPersistentStep(s);
            preparationRepository.add(persistentStep);
            LOGGER.debug("step {} updated to {}", s, persistentStep);
            counter.incrementAndGet();
        });
        LOGGER.info("Upgrade from {} to {} done, {} steps processed.", Step.class, PersistentStep.class, counter.get());

        LOGGER.debug("starting upgrade from {} to {}.", Preparation.class, PersistentPreparation.class);
        final Stream<Preparation> preparations = fileSystemPreparationRepository.list(Preparation.class);
        preparations.forEach(p -> {
            fileSystemPreparationRepository.remove(p);
            PersistentPreparation persistentPreparation = turnToPersistentPreparation(p);
            preparationRepository.add(persistentPreparation);
        });
        LOGGER.info("Upgrade from {} to {} done.", Preparation.class, PersistentPreparation.class);

        LOGGER.info("Migration of step ids in preparation...");
        final Stream<PersistentPreparation> persistentPreparations = preparationRepository.list(PersistentPreparation.class);
        persistentPreparations.forEach(p -> {
            LOGGER.info("Migration of preparation #{}", p.getId());
            final List<String> stepsIds = PreparationUtils.listStepsIds(p.getHeadId(), preparationRepository);
            p.setSteps(stepsIds);

            final DataSetMetadata metadata = dataSetMetadataRepository.get(p.getDataSetId());
            if (metadata != null) {
                LOGGER.info("Set metadata {} in preparation {}.", p.getDataSetId(), p.getId());
                p.setRowMetadata(metadata.getRowMetadata());
            } else {
                LOGGER.info("Metadata {} not found for preparation {}.", p.getDataSetId(), p.getId());
                p.setRowMetadata(new RowMetadata());
            }

            preparationRepository.add(p);
            LOGGER.info("Migration of preparation #{} done ({} steps)", p.getId(), stepsIds.size());
        });
        LOGGER.info("Migration of step ids in preparation done.");
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }

    private PersistentStep turnToPersistentStep(Step step) {
        PersistentStep result = new PersistentStep();
        result.setId(step.getId());
        result.setAppVersion(step.getAppVersion());
        result.setContent(step.getContent());
        result.setDiff(step.getDiff());
        result.setParentId(step.getParent());
        result.setRowMetadata(step.getRowMetadata());
        return result;
    }

    private PersistentPreparation turnToPersistentPreparation(Preparation preparation) {
        PersistentPreparation result = new PersistentPreparation();
        result.setId(preparation.getId());
        result.setAppVersion(preparation.getAppVersion());
        result.setAuthor(preparation.getAuthor());
        result.setCreationDate(preparation.getCreationDate());
        result.setDataSetId(preparation.getDataSetId());
        result.setHeadId(preparation.getHeadId());
        result.setLastModificationDate(preparation.getLastModificationDate());
        result.setName(preparation.getName());
        result.setRowMetadata(preparation.getRowMetadata());
        if (preparation.getSteps() != null)
            result.setSteps(preparation.getSteps().stream().map(Step::getId).collect(Collectors.toList()));
        return result;
    }

}
