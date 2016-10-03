// ============================================================================
//
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

package org.talend.dataprep.grants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NoOpAccessGrantCheckerTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
public class NoOpAccessGrantCheckerTest {

    @Autowired
    private AccessGrantChecker accessGrantChecker;

    @Test
    public void should_perform_certification_in_any_case() {
        RestrictedAction certification = CommonRestrictedActions.CERTIFICATION;
        assertEquals("CERTIFICATION", certification.action());
        assertTrue(accessGrantChecker.allowed(certification));
    }

}