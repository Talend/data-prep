package org.talend.dataprep.qa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Utility class for Integration Tests in Data-prep OS.
 */
@Component
public class OSIntegrationTestUtil {

    /**
     * Split a folder in a {@link Set} folder and subfolders.
     *
     * @param folder the folder to split.
     * @return a {@link Set} of folders and subfolders.
     */
    public Set<String> splitFolder(String folder) {
        Set<String> ret = new HashSet<>();
        if (folder == null || folder.isEmpty() || folder.equals("/"))
            return ret;
        String[] folders = folder.split("/");
        StringBuilder folderBuilder = new StringBuilder();
        Arrays.stream(folders) //
                .filter(f -> !f.isEmpty() && !f.equals("/")) //
                .forEach(f -> { //
                    if (folderBuilder.length() > 0) {
                        folderBuilder.append("/");
                    }
                    folderBuilder.append(f);
                    ret.add(folderBuilder.toString());
                });
        return ret;
    }

}
