// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.ServiceBaseTest;

public class WrapperProcessorTest extends ServiceBaseTest {

    @Test
    public void shouldProcessComponentWithType() throws Exception {
        assertTrue(ToBeProcessed.processed);
    }

    @Test
    public void shouldNotProcessComponentWithType() throws Exception {
        assertFalse(NotToBeProcessed.processed);
    }

    @Component
    public static class ToBeProcessed {

        static boolean processed = false;
    }

    @Component
    public static class NotToBeProcessed {

        static boolean processed = false;
    }

    @Component
    public static class ProcessedWrapper implements Wrapper<ToBeProcessed> {

        @Override
        public Class<ToBeProcessed> wrapped() {
            return ToBeProcessed.class;
        }

        @Override
        public ToBeProcessed doWith(ToBeProcessed instance, String beanName, ApplicationContext applicationContext) {
            ToBeProcessed.processed = true;
            return instance;
        }
    }
}
