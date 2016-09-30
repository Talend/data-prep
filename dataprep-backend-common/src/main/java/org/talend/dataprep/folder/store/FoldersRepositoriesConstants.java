package org.talend.dataprep.folder.store;

import org.talend.dataprep.folder.store.file.FolderPath;

public class FoldersRepositoriesConstants {

    /**
     * Path separator used to separate path elements on string serialization.
     * <p>Should only be used by {@link FolderPath#serializeAsString()}
     * and {@link FolderPath#deserializeFromString(String)}.
     */
    public static final Character PATH_SEPARATOR = '/';

    /** Constant for HOME_FOLDER. */
    public static final String HOME_FOLDER_KEY = "HOME";

    /** Constant for the content type string. */
    public static final String CONTENT_TYPE = "contentType";

    /** Constant for the content id string. */
    public static final String CONTENT_ID = "contentId";

    /** Constant to prevent string literal duplication. */
    public static final String FOLDER_ID = "folderId";

    /** Constant to prevent string literal duplication. */
    public static final String OWNER_ID = "ownerId";

    /** Constant to prevent string literal duplication. */
    public static final String PATH = "path";
}
