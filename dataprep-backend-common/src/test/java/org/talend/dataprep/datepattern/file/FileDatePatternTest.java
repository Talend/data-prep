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
package org.talend.dataprep.datepattern.file;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.datepattern.AbstractDatepatternTest;
import org.talend.dataprep.datepattern.DatePatternRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileDatePatternTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "datepattern.store=file",
        "datepattern.store.file.location=target/test/store/datepattern" })
public class FileDatePatternTest extends AbstractDatepatternTest {

    @Inject
    @Named("datePatternRepository#file")
    private DatePatternRepository datePatternRepository;

    @Override
    protected DatePatternRepository getDatePatternRepository() {
        return datePatternRepository;
    }
}
