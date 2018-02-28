// ============================================================================
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

package org.talend.dataprep.async;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.async.progress.ExecutionContext;

/**
 * Mock controller used by the AsyncAspect unit test.
 *
 * @see AsyncAspectTest
 */
@RestController
public class AsyncAspectTestMockController {

    static final AtomicBoolean cancelled = new AtomicBoolean(false);

    static final AtomicBoolean stopped = new AtomicBoolean(false);

    @AsyncOperation
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/noGroupName")
    public String noGroupName() {
        return "no group name !";
    }

    @AsyncOperation
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/groupNameFromAnnotation")
    public String groupNameFromAnnotation(String toto, @AsyncGroupId String groupName) {
        return "group name is " + groupName;
    }

    @AsyncOperation(groupIdGeneratorBean = ChuckNorrisGroupIdGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/groupNameFromSpringBean")
    public String groupNameFromSpringBean(int anyNumber) {
        return "group name is defined by a spring bean";
    }

    @AsyncOperation(groupIdGeneratorBean = InvalidGroupIdGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/invalidSpringBean")
    public String invalidSpringBeanDefinition() {
        return "group name cannot be defined by a spring bean";
    }

    @AsyncOperation(groupIdGeneratorClass = InvalidGroupIdGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/invalidClassGenerator")
    public String invalidGeneratorClass() {
        return "group name cannot be defined defined by a class";
    }

    @AsyncOperation
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/groupNameFromAsyncGroupKeyAnnotation")
    public String groupNameFromAsyncGroupKeyAnnotation(@AsyncGroupId ExportParameters params) {
        return "group name from export parameters";
    }

    @AsyncOperation
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/executionIdFromPath/{executionId}")
    public String executionIdFromPath(@AsyncExecutionId @PathVariable(name = "executionId") String executionId) {
        return "group name from export parameters";
    }

    @AsyncOperation(groupIdGeneratorClass = CancelAndStopGroupIdGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/cancel")
    public void callCancel() {
        try {
            ExecutionContext.get().on((execution, signal) -> {
                switch (signal) {
                case CANCEL:
                    cancelled.set(true);
                }
            });
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AsyncOperation(groupIdGeneratorClass = CancelAndStopGroupIdGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/aspect/test/mock/stop")
    public void callStop() {

        // set the stop behavior
        ExecutionContext.get().on((execution, signal) -> {
            switch (signal) {
            case STOP:
                stopped.set(true);
            }
        });

        try {
            // execution last 1000 ms
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AsyncOperation(conditionalClass = PairConditionalAsyncTest.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/conditional/test/")
    public String asyncOnlyPairNumber(@AsyncParameter Integer nb) {
        return "ok";
    }

    @AsyncOperation(resultUrlGenerator = MockResultUrlGenerator.class)
    @RequestMapping(method = RequestMethod.GET, path = "/async/url/generate")
    public String generateResultUrl(@AsyncParameter Integer nb) {
        return "ok";
    }
}
