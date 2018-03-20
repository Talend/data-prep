package org.talend.dataprep.async;

import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.List;

import static java.util.Comparator.naturalOrder;
import static org.junit.Assert.*;

public class AsyncExecutionTest {

    @Test
    public void shouldOrderRunningFirst() {
        List<AsyncExecution> unOrderedAsyncs = Lists.newArrayList(
                buildAsyncExecution(AsyncExecution.Status.NEW, 100L),
                buildAsyncExecution(AsyncExecution.Status.DONE, 100L),
                buildAsyncExecution(AsyncExecution.Status.RUNNING, 100L),
                buildAsyncExecution(AsyncExecution.Status.CANCELLED, 100L),
                buildAsyncExecution(AsyncExecution.Status.FAILED, 100L)
        );

        unOrderedAsyncs.sort(naturalOrder());

        assertEquals(AsyncExecution.Status.RUNNING, unOrderedAsyncs.get(0).getStatus());
    }

    @Test
    public void shouldOrderMostRecentEndFirst() {
        long mostRecentEndDate = 35374L;
        List<AsyncExecution> unOrderedAsyncs = Lists.newArrayList(
                buildAsyncExecution(AsyncExecution.Status.DONE, 25374L),
                buildAsyncExecution(AsyncExecution.Status.DONE, mostRecentEndDate)
        );

        unOrderedAsyncs.sort(naturalOrder());

        assertEquals(mostRecentEndDate, unOrderedAsyncs.get(0).getTime().getEndDate());
    }

    private AsyncExecution buildAsyncExecution(AsyncExecution.Status aNew, long endDate) {
        AsyncExecution newAsyncExecution = new AsyncExecution();
        newAsyncExecution.setStatus(aNew);
        AsyncExecution.Time time = new AsyncExecution.Time();
        time.setEndDate(endDate);
        newAsyncExecution.setTime(time);
        return newAsyncExecution;
    }

}
