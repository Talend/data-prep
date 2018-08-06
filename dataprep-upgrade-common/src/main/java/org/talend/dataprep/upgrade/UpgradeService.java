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

package org.talend.dataprep.upgrade;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.USER;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.upgrade.model.UpgradeTask;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;

/**
 * Service in charge of upgrading data from data prep previous versions.
 */
@Service
public class UpgradeService {

    /** This class' logger. */
    private static final Logger LOG = getLogger(UpgradeService.class);

    /** The upgrade task repository. */
    @Autowired
    private UpgradeTaskRepository repository;

    /** All the available upgrade tasks (default is an empty list). */
    @Autowired(required = false)
    private List<UpgradeTask> tasks = emptyList();

    /**
     * Sort the tasks and make sure there is no conflict in the version / order.
     */
    @PostConstruct
    void sortAndCheckTasks() { // NOSONAR called by @PostConstruct annotation
        Collections.sort(tasks);

        Map<String, List<Integer>> groupTasks = new HashMap<>();
        List<UpgradeTaskId> conflictTasks = new ArrayList<>();
        tasks.stream().map(UpgradeTask::getId).forEach(id -> {
            // add the version entry if needed
            if (!groupTasks.containsKey(id.getVersion())) {
                groupTasks.put(id.getVersion(), new ArrayList<>());
            }
            // check if there's already a task for this order
            final List<Integer> orders = groupTasks.get(id.getVersion());
            if (orders.contains(id.getOrder())) {
                conflictTasks.add(id);
            } else {
                orders.add(id.getOrder());
            }
        });

        if (!conflictTasks.isEmpty()) {
            throw new IllegalArgumentException("The are " + conflictTasks.size()
                    + " conflicted upgrade tasks (task with the same order for one version) : " + conflictTasks);
        }
    }

    /**
     * Are all available migration tasks are applied ?
     *
     * @return <code>true</code> if all available tasks are already applied, <code>false</code> else.
     */
    public boolean needUpgrade() {
        int appliedTasks = repository.countUpgradeTask(VERSION.name());
        int availableTasks = (int) tasks.stream().filter(task -> Objects.equals(task.getTarget(), VERSION)).count();
        if (appliedTasks > availableTasks) {
            LOG.warn("It seems that more upgrade tasks have been applied than the available ones.");
            return true;
        } else if (appliedTasks == availableTasks) {
            return false;
        } else { // appliedTasks < availableTasks
            return true;
        }
    }

    /**
     * Apply all upgrade tasks that need to be applied once per version.
     */
    public void upgradeVersion() {

        LOG.info("Global upgrade process starting");

        int alreadyApplied = 0;
        int numberOfTasksApplied = 0;
        for (UpgradeTask task : tasks) {

            final UpgradeTaskId taskId = task.getId();

            // skip non version upgrade task
            if (task.getTarget() != VERSION) {
                LOG.debug("{} does not target version", taskId);
                continue;
            }

            final String targetId = VERSION.name() + '-' + taskId.getVersion();

            if (repository.isAlreadyApplied(targetId, taskId)) {
                LOG.debug("{} already applied, let's skip it", taskId);
                alreadyApplied++;
            } else {
                LOG.debug("apply upgrade {}", taskId);
                try {
                    task.run();
                } catch (Exception exception) {
                    LOG.error("Failed to apply upgrade {}", taskId, exception);
                    break;
                }
                repository.applied(targetId, taskId);
                numberOfTasksApplied++;
            }
        }
        LOG.info("Global upgrade process finished, {}/{} upgrade(s) performed ({} already applied).", numberOfTasksApplied, tasks.size(), alreadyApplied);
    }

    public void upgradeUser(String userId) {

        LOG.info("Upgrade process starting for user {}", userId);

        int numberOfTasksApplied = 0;
        for (UpgradeTask task : tasks) {

            // skip non user upgrade task
            if (task.getTarget() != USER) {
                LOG.debug("{} does not target users", task.getId());
                continue;
            }

            final String targetId = USER.name() + '-' + userId;

            if (repository.isAlreadyApplied(targetId, task.getId())) {
                LOG.debug("{} already applied for user {}, let's skip it", task.getId(), userId);
            } else {
                LOG.debug("apply upgrade {} for user {}", task.getId(), userId);
                task.run();
                repository.applied(targetId, task.getId());
                numberOfTasksApplied++;
            }

        }
        LOG.info("Upgrade process finished for user {}, {}/{} upgrade(s) performed", userId, numberOfTasksApplied, tasks.size());
    }

    void setTasks(List<UpgradeTask> tasks) {
        this.tasks = tasks;
    }
}
