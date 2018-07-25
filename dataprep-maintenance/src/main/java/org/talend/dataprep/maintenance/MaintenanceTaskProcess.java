package org.talend.dataprep.maintenance;

import java.util.function.Supplier;

public abstract class MaintenanceTaskProcess {

    /**
     * Execute maintenance task only if condition is TRUE
     */
    public void execute() {
        if( this.condition().get()){
            this.performTask();
        }
    }

    protected abstract void performTask();

    protected abstract Supplier<Boolean> condition();

}
