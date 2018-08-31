// ============================================================================
//
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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.PostConstruct;

/**
 * Due to TDP-3303, the replace value was split to have 2 implementations : one for the whole column and one for a
 * single cell.
 */
@Component
public class SplitReplaceOnValueAction implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(SplitReplaceOnValueAction.class);

    private static final String REPLACE_ON_VALUE = "replace_on_value";

    private static final String CELL = "cell";

    static final String CELL_VALUE = "cell_value";

    static final String REPLACE_VALUE = "replace_value";

    static final String REPLACE_CELL_VALUE = "replace_cell_value";

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        mapper.registerModule(new MixedContentMapModule());
    }

    @Override
    public void run() {

        final Stream<PreparationActions> preparationActionsStream =
                preparationRepository.list(PreparationActions.class).filter(p -> p.getActions().stream().anyMatch(
                        a -> REPLACE_ON_VALUE.equals(a.getName()) && CELL.equals(a.getParameters().get("scope"))));

        AtomicLong actionsUpdated = new AtomicLong(0);
        preparationActionsStream.forEach(currentPrepActions -> {
            final List<Action> actions = currentPrepActions.getActions();
            boolean updatePrepActions = false;
            for (Action action : actions) {
                final Map<String, String> parameters = action.getParameters();
                // only deal with replace_on_value with scope cell
                if (REPLACE_ON_VALUE.equals(action.getName()) && CELL.equals(parameters.get("scope"))) {
                    // rename action
                    action.setName(REPLACE_CELL_VALUE);
                    // update parameters
                    updateActionParameters(parameters);
                    updatePrepActions = true;
                }
            }
            // only save updated preparation actions
            if (updatePrepActions) {
                preparationRepository.add(currentPrepActions);
                actionsUpdated.getAndIncrement();
            }
        });

        LOGGER.info("'Replace On Value' implementations were successfully split, {} action(s) updated", actionsUpdated);

    }

    /**
     * Update the action parameters :
     * <ul>
     * <li>extract the token value from 'cell_value' and set it to 'original_value'</li>
     * <li>rename the 'replace_value' to 'new_value'</li>
     * </ul>
     *
     * @param parameters the actions parameters.
     */
    private void updateActionParameters(Map<String, String> parameters) {

        JsonNode cellValueNode;
        final String cellValue = parameters.get(CELL_VALUE);
        try {
            cellValueNode = mapper.readTree(cellValue);
            if (cellValueNode == null) {
                cellValueNode = buildDefaultCellValueNode(cellValue);
            }
        } catch (IOException e) {
            cellValueNode = buildDefaultCellValueNode(cellValue);
        }

        final String originalValue = cellValueNode.get("token").asText();
        parameters.put("original_value", originalValue);
        parameters.remove(CELL_VALUE);

        parameters.put("new_value", parameters.get(REPLACE_VALUE));
        parameters.remove(REPLACE_VALUE);
    }

    private JsonNode buildDefaultCellValueNode(String cellValue) {
        JsonNode cellValueNode = mapper.createObjectNode();
        ((ObjectNode) cellValueNode).put("token", cellValue);
        LOGGER.warn("Could not get the original value out of '{}', let's use it as is", cellValue);
        return cellValueNode;
    }

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public UpgradeTask.target getTarget() {
        return VERSION;
    }
}
