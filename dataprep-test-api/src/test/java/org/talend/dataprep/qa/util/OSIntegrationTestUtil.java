package org.talend.dataprep.qa.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.dto.Folder;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.talend.dataprep.qa.config.FeatureContext.suffixFolderName;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.SCOPE;

/**
 * Utility class for Integration Tests in Data-prep OS.
 */
@Component
public class OSIntegrationTestUtil {

    List<String> parametersToBeSuffixed = Arrays.asList("new_domain_id", "new_domain_label");

    /**
     * Split a folder in a {@link Set} folder and subfolders.
     *
     * @param folder  the folder to split.
     * @param folders existing folders.
     * @return a {@link Set} of folders and subfolders.
     */
    public Set<Folder> splitFolder(Folder folder, List<Folder> folders) {
        Set<Folder> ret = new HashSet<>();
        if (folder == null || folder.getPath().equals("/"))
            return ret;

        final Map<String, Folder> folderByPath = new HashMap<>(folders.size());
        folders.forEach(f -> folderByPath.put(f.getPath().substring(1), f));

        String[] folderPaths = folder.getPath().split("/");
        StringBuilder folderBuilder = new StringBuilder();
        Arrays
                .stream(folderPaths) //
                .filter(f -> !f.isEmpty() && !f.equals("/")) //
                .forEach(f -> { //
                    if (folderBuilder.length() > 0) {
                        folderBuilder.append("/");
                    }
                    folderBuilder.append(f);
                    Folder tmpF = folderByPath.get(folderBuilder.toString());
                    if (tmpF != null) {
                        ret.add(tmpF);
                    }
                });
        return ret;
    }

    /**
     * Map parameters from a Cucumber step to an Action parameters.
     * <p>
     * add default scope column
     * </p>
     *
     * @param params the parameters to map.
     * @return the given {@link Action} updated.
     */
    @NotNull
    public Map<String, Object> mapParamsToActionParameters(@NotNull Map<String, String> params) {
        Map<String, Object> actionParameters = params
                .entrySet()
                .stream() //
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    if (parametersToBeSuffixed.contains(e.getKey())) {
                        return suffixName(e.getValue());
                    } else {
                        return StringUtils.isEmpty(e.getValue()) ? null : e.getValue();
                    }
                }));

        actionParameters.putIfAbsent(SCOPE.getKey(), "column");

        return actionParameters;
    }

    /**
     * Extract an extension from a filename. If no extension present, the filename is returned.
     *
     * @param filename the filename.
     * @return the filename's extension.
     */
    @NotNull
    public String getFilenameExtension(@NotNull String filename) {
        String[] tokens = filename.split("\\.");
        return tokens[tokens.length - 1];
    }

    /**
     * Return the path of a fully qualified name.
     *
     * @param fullName the fully qualified name.
     * @return the path or an empty {@link String } if no path is found.
     */
    @NotNull
    public String extractPathFromFullName(@NotNull String fullName) {
        String foundPath = "/";
        if (fullName.contains("/") && fullName.lastIndexOf("/") != 0) {
            foundPath = fullName.substring(0, fullName.lastIndexOf("/"));
        }
        return suffixFolderName(foundPath);
    }

    /**
     * Return the name of a fully qualified name.
     *
     * @param fullName the fully qualified name.
     * @return the found name.
     */
    @NotNull
    public String extractNameFromFullName(@NotNull String fullName) {
        return fullName.substring(fullName.lastIndexOf("/") + 1);
    }
}
