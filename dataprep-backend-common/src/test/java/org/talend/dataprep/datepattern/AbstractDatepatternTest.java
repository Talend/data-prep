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

package org.talend.dataprep.datepattern;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

/**
 * Abstract Test class for {@link DatePattern} management
 */
public abstract class AbstractDatepatternTest
{

    protected abstract DatePatternRepository getDatePatternRepository();

    @After
    public void cleanAfter() {
        getDatePatternRepository().clear();
    }

    @Test
    public void add_one_list_one_then_remove() {

        Assertions.assertThat(getDatePatternRepository().all()).isNotNull().isEmpty();
        DatePattern datePattern = new DatePattern("yyyy MMM dd");
        getDatePatternRepository().add(datePattern);

        Assertions.assertThat(getDatePatternRepository().all()) //
                .isNotNull() //
                .isNotEmpty() //
                .hasSize(1) //
                .contains(datePattern);

        getDatePatternRepository().remove(datePattern);
        Assertions.assertThat(getDatePatternRepository().all()).isNotNull().isEmpty();

    }

    @Test
    public void add_two_list_then_remove() {

        Assertions.assertThat(getDatePatternRepository().all()).isNotNull().isEmpty();

        DatePattern first = new DatePattern("yyyy MMM dd");
        DatePattern second = new DatePattern("yy M dd");
        getDatePatternRepository().add(first);
        getDatePatternRepository().add(second);
        getDatePatternRepository().add(first);

        Assertions.assertThat(getDatePatternRepository().all()) //
            .isNotNull() //
            .isNotEmpty() //
            .hasSize(2) //
            .contains(first, second);

        getDatePatternRepository().remove(first);
        Assertions.assertThat(getDatePatternRepository().all()) //
            .isNotNull() //
            .isNotEmpty() //
            .hasSize(1) //
            .contains(second);

        getDatePatternRepository().remove(second);
        Assertions.assertThat(getDatePatternRepository().all()).isNotNull().isEmpty();

    }

}
