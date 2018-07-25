package org.talend.dataprep.qa.dto;

import java.util.List;
import java.util.stream.IntStream;

import org.talend.dataprep.helper.api.Action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Preparation details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparationDetails extends NamedItem {

    public List<String> steps;

    public List<Action> actions;

    public Folder folder;

    public String dataSetId;

    /**
     * Update the {@link Action#id} from the steps information.
     */
    public void updateActionIds() {
        IntStream.range(0, steps.size() - 1).forEach(i -> actions.get(i).id = steps.get(i + 1));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Folder extends NamedItem {

        public String parentId;

        public String path;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dataset {

        public String dataSetId;

        public String dataSetName;

        public String dataSetNbRow;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public String getHead() {
        if (steps == null || steps.isEmpty()) {
            return "head";
        }
        else {
            return steps.get(steps.size() - 1);
        }
    }
}
