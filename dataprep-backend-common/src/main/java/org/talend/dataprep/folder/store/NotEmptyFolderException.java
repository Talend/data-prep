package org.talend.dataprep.folder.store;

/**
 * This exception happen whern trying to delete a non empty folder
 */
public class NotEmptyFolderException extends Exception {

    public NotEmptyFolderException(String message) {
        super(message);
    }
}
