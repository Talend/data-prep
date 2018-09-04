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

package org.talend.dataprep.upgrade.repository.file;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;
import org.talend.dataprep.upgrade.repository.model.AppliedUpgradeTask;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * File system task repository.
 */
@Component
@ConditionalOnProperty(name = "upgrade.store", havingValue = "file")
public class FileUpgradeTaskRepository implements UpgradeTaskRepository {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FileUpgradeTaskRepository.class);

    /** The dataprep ready jackson builder. */
    @Autowired
    private ObjectMapper mapper;

    /** Where to store the dataset metadata */
    @Value("${upgrade.store.file.location}")
    private String storeLocation;

    /**
     * @see UpgradeTaskRepository#isAlreadyApplied(String, UpgradeTaskId)
     */
    @Override
    public boolean isAlreadyApplied(String targetId, UpgradeTaskId id) {

        final Path start = getRootFolder(targetId).toPath();
        int maxDepth = 1;
        try (Stream<Path> stream =
                Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).endsWith(".json"))) {
            final boolean found = stream //
                    .filter(p -> StringUtils.startsWith(p.getFileName().toString(), id.getUniqueKey())) //
                    .findFirst()
                    .isPresent();

            LOG.debug("{} {} for {}", id, found ? "found" : "not found", targetId);

            return found;
        } catch (IOException e) {
            LOG.warn("Error looking for " + id.getUniqueKey() + " : " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * @see UpgradeTaskRepository#applied(String, UpgradeTaskId)
     */
    @Override
    public void applied(String targetId, UpgradeTaskId id) {
        AppliedUpgradeTask task = new AppliedUpgradeTask(id);
        File file = new File(getRootFolder(targetId), id.getUniqueKey() + ".json");
        try {
            mapper.writerFor(AppliedUpgradeTask.class).writeValue(file, task);
        } catch (IOException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        LOG.debug("{} for {} saved here {}", task, targetId, file.getPath());
    }

    @Override
    public int countUpgradeTask(String targetPrefix) {
        int nbUpgradeTasks = 0;
        final File rootFolder = new File(storeLocation);
        if (rootFolder.exists()) {
            final Path start = rootFolder.toPath();
            int maxDepth = 2;
            try (Stream<Path> stream =
                    Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).endsWith(".json"))) {
                nbUpgradeTasks = (int) stream.count();
            } catch (IOException e) {
                LOG.error("Unable to count upgraded tasks from {}", rootFolder.toString());
            }
        }
        return nbUpgradeTasks;
    }

    /**
     *
     * @param userId the current user id.
     * @return the root folder.
     */
    private File getRootFolder(String userId) {
        final File userRootFolder = new File(storeLocation + '/' + userId + '/');
        try {
            // create the folder is it does not exists
            if (!userRootFolder.exists()) {
                Files.createDirectories(userRootFolder.toPath());
            }
        } catch (IOException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        return userRootFolder;
    }

}
