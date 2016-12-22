//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
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
        DataSetMetadata dsm1 = metadataBuilder.metadata() //
                .id("1") //
                .name("one") //
                .author("jim") //
                .created(12)
                .modified(12)
                .build();
        DataSetMetadata dsm2 = metadataBuilder.metadata() //
                .id("1") //
                .name("theone") //
                .author("jimmy") //
                .created(12)
                .modified(12)
                .build();
        final AtomicLong threadCount = new AtomicLong(2);
        new Thread(() -> {
            try {
                DistributedLock datasetLock = metadataRepository.createDatasetMetadataLock("1"); //$NON-NLS-1$
                datasetLock.lock();
                try {
                    Thread.sleep(1000);
                    metadataRepository.save(dsm1);
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
                metadataRepository.save(dsm2);
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
