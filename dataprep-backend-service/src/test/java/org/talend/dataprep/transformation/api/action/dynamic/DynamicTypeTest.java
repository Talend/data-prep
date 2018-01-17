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

package org.talend.dataprep.transformation.api.action.dynamic;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.transformation.api.action.dynamic.cluster.ClusterParameters;

/**
 * Unit test for the dynamic type.
 */
public class DynamicTypeTest extends ServiceBaseTest {

    /** Spring application context. */
    @Autowired
    private ApplicationContext context;

    @Test
    public void shouldReturnNull() {
        assertNull(DynamicType.fromAction(null));
        assertNull(DynamicType.fromAction("blahblah"));
    }

    @Test
    public void shouldReturnCluster() {
        final DynamicType actual = DynamicType.fromAction("textclustering");
        assertEquals(DynamicType.TEXT_CLUSTER, actual);
    }

    @Test
    public void shouldReturnClusterType() {
        final DynamicParameters actual = DynamicType.fromAction("textclustering").getGenerator(context);
        assertTrue(actual instanceof ClusterParameters);
    }

}
