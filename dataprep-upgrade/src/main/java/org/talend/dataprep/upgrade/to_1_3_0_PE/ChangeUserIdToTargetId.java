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

package org.talend.dataprep.upgrade.to_1_3_0_PE;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.emptySet;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * Because upgrade tasks not only apply to user anymore, the userId needs to be changed to targetId.
 *
 * @see org.talend.dataprep.upgrade.model.UpgradeTask.target
 */
@Component
public class ChangeUserIdToTargetId implements BaseUpgradeTaskTo_1_3_0_PE {

    /** This class' logger. */
    private static final Logger LOG = getLogger(ChangeUserIdToTargetId.class);

    /** Root folder where are stored the upgrade files. */
    @Value("${upgrade.store.file.location}")
    private String storeLocation;

    /**
     * @see UpgradeTask#getOrder()
     */
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }

    /**
     * @see UpgradeTask#run()
     */
    @Override
    public void run() {

        Path rootFolder = Paths.get(storeLocation);
        try {
            Files.walkFileTree(rootFolder, emptySet(), 1, new RenameUserToTarget());
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

    }

    static class RenameUserToTarget extends SimpleFileVisitor<Path> {

        /**
         * Rename the current directory.
         *
         * @see SimpleFileVisitor#visitFile(Object, BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isDirectory() && !StringUtils.startsWith(file.getFileName().toString(), target.VERSION.name())) {
                String userName = file.getFileName().toString();
                String targetName = target.USER.name() + '-' + userName;
                Files.move(file, Paths.get(file.getParent().toAbsolutePath().toString(), targetName), REPLACE_EXISTING);
                LOG.debug("{} renamed to {}");
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
