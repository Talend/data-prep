package org.talend.dataprep.qa.util;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.SCOPE;

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

    private static final List<String>
            PARAMETERS_TO_BE_SUFFIXED = Arrays.asList("new_domain_id", "new_domain_label", "lookup_ds_name");

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
     * Map parameters from a Cucumber step to an Action parameters.
     * <p>add default scope column</p>
     *
     * @param params the parameters to map.
     * @return the given {@link Action} updated.
     */
    @Nullable
    public Map<String, Object> mapParamsToActionParameters(@NotNull Map<String, String> params) {
        Map<String, Object> actionParameters = new HashMap<>();
        params.forEach((k, v) ->
                ActionParamEnum.getActionParamEnum(k)
                        .ifPresent(actionParamEnum -> {
                            Object value;
                            if (PARAMETERS_TO_BE_SUFFIXED.contains(actionParamEnum.getName())) {
                                value = suffixName(v);
                            } else {
                                value = StringUtils.isEmpty(v) ? null : v;
                            }
                            actionParameters.put(actionParamEnum.getJsonName(), value);
                        }));
        actionParameters.put(FILTER.getKey(), mapParamsToFilter(params));
        actionParameters.putIfAbsent(SCOPE.getKey(), "column");

        return actionParameters;
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
                .map(ActionFilterEnum::getActionFilterEnum) //
                .filter(Objects::nonNull) //
                .peek(afe -> {
                    String v = params.get(afe.getName());
                    filter.range.put(afe, afe.processValue(v));
                }) //
                .count();
        return nbAfes > 0 ? filter : null;
    }

}
