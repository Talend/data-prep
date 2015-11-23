// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.lock.DistributedLock;

public class MetadataRepositoryLockTest extends DataSetBaseTest {

    @Autowired
    ApplicationContext appContext;

    @Test
    public void test() throws InterruptedException {
        // I am not sure of the validity of this test as it tests the DistributedLock that is already provided by a
        // third party lib.
        DataSetMetadataRepository metadataRepository = appContext.getBean(DataSetMetadataRepository.class);
        DataSetMetadata dsm1 = new DataSetMetadata("1", "one", "jim", 12, new RowMetadata()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        DataSetMetadata dsm2 = new DataSetMetadata("1", "theone", "jimmy", 12, new RowMetadata()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dsm2.setRowMetadata(new RowMetadata());
        final AtomicLong threadCount = new AtomicLong(2);
        new Thread(() -> {
            try {
                DistributedLock datasetLock = metadataRepository.createDatasetMetadataLock("1"); //$NON-NLS-1$
                datasetLock.lock();
                try {
                    Thread.sleep(1000);
                    metadataRepository.add(dsm1);
                    threadCount.decrementAndGet();
                } finally {
                    datasetLock.unlock();
                }
            } catch (InterruptedException e) {
                // should never be interrupted
            }

        }, "Test Lock DatasetMetadata 1").start(); //$NON-NLS-1$
        Thread.sleep(500);// let the previous thread start.
        // the second thread should add dsm2 first without lock implementation but be second with the lock
        // implementation
        new Thread(() -> {
            DistributedLock datasetLock = metadataRepository.createDatasetMetadataLock("1"); //$NON-NLS-1$
            datasetLock.lock();
            try {
                metadataRepository.add(dsm2);
                threadCount.decrementAndGet();
            } finally {
                datasetLock.unlock();
            }
        }, "Test Lock DatasetMetadata 2").start(); //$NON-NLS-1$

        int count = 1000;// to prevent the loop for never ending, this is a timeout of 10s
        while ((threadCount.get() != 0) && (count != 0)) {
            Thread.sleep(10);
            count--;
        }

        assertNotEquals("the Test has timed out", 0, count); //$NON-NLS-1$
        assertEquals(dsm2, metadataRepository.get("1")); //$NON-NLS-1$
    }
}
