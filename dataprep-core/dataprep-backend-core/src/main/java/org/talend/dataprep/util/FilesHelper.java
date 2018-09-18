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

package org.talend.dataprep.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesHelper.class);

    private FilesHelper() {
    }

    /**
     * Similarly to {@link #delete(File)} but will catch all exceptions related to the delete operation.
     * 
     * @param file The file to be deleted, <code>null</code> returns immediately.
     */
    public static void deleteQuietly(File file) {
        try {
            delete(file);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to delete file {}.", file, e);
            } else {
                LOGGER.warn("Unable to delete file {}.", file);
            }
        }
    }

    /**
     * Delete a file:
     * <ol>
     * <li>First using {@link File#delete()}</li>
     * <li>If failed to, use {@link java.nio.file.Files#delete(Path)}</li>
     * <li>If failed to, performs a {@link System#gc()} and retry {@link java.nio.file.Files#delete(Path)} (see
     * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154">issue</a>)</li>
     * </ol>
     * 
     * @param file The file to be deleted, <code>null</code> returns immediately.
     * @throws IOException If file can't be deleted.
     */
    public static void delete(File file) throws IOException {
        if (file == null) {
            return;
        }
        if (file.getName().startsWith(".nfs")) {
            // Ignore NFS specific files
            return;
        }
        if (file.exists()) {
            if (!file.delete()) {
                final Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
                try {
                    LOGGER.debug("Unable to delete file {} using IO, using NIO for more detailed error.", file);
                    java.nio.file.Files.delete(path);
                } catch (IOException e) {
                    // See http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154, give it a second try after GC
                    LOGGER.debug("Unable to delete file {} using NIO, forcing GC.", file, e);
                    System.gc();
                    java.nio.file.Files.delete(path);
                }
            }
        }
    }

}
