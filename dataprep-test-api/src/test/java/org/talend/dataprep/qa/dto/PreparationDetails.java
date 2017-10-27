package org.talend.dataprep.qa.dto;

import java.util.List;
import java.util.stream.IntStream;

import org.talend.dataprep.helper.api.Action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Preparation details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparationDetails {

    public List<String> steps;

    public List<Action> actions;

    /**
     * Update the {@link Action#id} from the steps information.
     */
    public void updateActionIds() {
        IntStream.range(0, steps.size() - 1).forEach(i -> actions.get(i).id = steps.get(i + 1));
    }
}
