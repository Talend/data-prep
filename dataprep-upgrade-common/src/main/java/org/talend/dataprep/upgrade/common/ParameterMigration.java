package org.talend.dataprep.upgrade.common;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.tql.model.Expression;

public final class ParameterMigration {

    private static final Logger LOGGER = getLogger(ParameterMigration.class);

    private ParameterMigration() {
    }

    public static void upgradeParameters(PreparationRepository repository, Consumer<Action> updater) {
        upgradeParameters(repository, null, updater);
    }

    public static void upgradeParameters(PreparationRepository repository, Expression filter,
            Consumer<Action> updater) {
        final Stream<PreparationActions> stream;
        if (filter != null) {
            stream = repository.list(PreparationActions.class, filter);
        } else {
            stream = repository.list(PreparationActions.class);
        }

        stream
                .filter(pa -> !PreparationActions.ROOT_ACTIONS.id().equals(pa.id()) && pa.getActions() != null
                        && !pa.getActions().isEmpty())
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(updater);
                    action.setId(null);
                    final String afterUpdateId = action.id();

                    if (!beforeUpdateId.equals(afterUpdateId)) {
                        LOGGER.debug("Migration changed action id from '{}' to '{}', updating steps", beforeUpdateId,
                                afterUpdateId);
                        repository
                                .list(PersistentStep.class, eq("contentId", beforeUpdateId)) //
                                .filter(s -> !Step.ROOT_STEP.id().equals(s.id())) //
                                .peek(s -> s.setContent(afterUpdateId)) //
                                .forEach(repository::add);
                    }
                }) //
                .forEach(repository::add); //
    }

}
