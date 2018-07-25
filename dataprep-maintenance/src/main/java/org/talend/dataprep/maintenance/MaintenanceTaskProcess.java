package org.talend.dataprep.maintenance;

import java.util.function.Supplier;

public interface MaintenanceTaskProcess {

    void executeTask();

    Supplier<Boolean> conditionTask();

}
