package org.talend.dataprep.maintenance.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TaskScheduler {

    @Autowired
    private List<MaintenanceTask> maintenanceTasks;

    @Autowired
    private GroupedForAll forAll;

    @PostConstruct
    private void launchOnce(){
        forAll.execute(() -> true, new Runnable() {
            @Override
            public void run() {
                // filter maintenance task marked as ONCE and run it
            }
        });
    }


    private void launchNightly(){

    }

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000) // Every hour
    private void launchRepeatly(){

    }

}
