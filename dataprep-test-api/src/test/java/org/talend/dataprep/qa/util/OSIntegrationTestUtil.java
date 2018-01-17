package org.talend.dataprep.qa.util;

import static org.talend.dataprep.helper.api.ActionParamEnum.FILTER;
import static org.talend.dataprep.helper.api.ActionParamEnum.SCOPE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionFilterEnum;
import org.talend.dataprep.helper.api.ActionParamEnum;
import org.talend.dataprep.helper.api.Filter;
import org.talend.dataprep.qa.dto.Folder;

/**
 * Utility class for Integration Tests in Data-prep OS.
 */
@Component
public class OSIntegrationTestUtil {

    public static final String ACTION_NAME = "actionName";

    /**
     * Split a folder in a {@link Set} folder and subfolders.
     *
     * @param folder the folder to split.
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
        Arrays.stream(folderPaths) //
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
     * Map parameters from a Cucumber step to an {@link Action}.
     *
     * @param params the parameters to map.
     * @param action the {@link Action} that will receive the parameters.
     * @return the given {@link Action} updated.
     */
    @NotNull
    public Action mapParamsToAction(@NotNull Map<String, String> params, @NotNull Action action) {
        action.action = params.get(ACTION_NAME) == null ? action.action : params.get(ACTION_NAME);
        params.forEach((k, v) -> {
            ActionParamEnum ape = ActionParamEnum.getActionParamEnum(k);
            if (ape != null) {
                action.parameters.put(ape, StringUtils.isEmpty(v) ? null : v);
            }
        });
        Filter filter = mapParamsToFilter(params);
        action.parameters.put(FILTER, filter);
        action.parameters.putIfAbsent(SCOPE, "column");
        return action;
    }

    /**
     * Map parameters from a Cucumber step to an {@link Filter}.
     *
     * @param params the parameters to map.
     * @return a filled {@link Filter} or <code>null</code> if no filter found.
     */
    @Nullable
    public Filter mapParamsToFilter(@NotNull Map<String, String> params) {
        final Filter filter = new Filter();
        long nbAfes = params.keySet().stream() //
                .map(k -> ActionFilterEnum.getActionFilterEnum(k)) //
                .filter(Objects::nonNull) //
                .peek(afe -> {
                    String v = params.get(afe.getName());
                    filter.range.put(afe, afe.processValue(v));
                }) //
                .count();
        return nbAfes > 0 ? filter : null;
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
}
