package org.talend.dataprep.util;

import org.junit.Test;
import org.talend.dataprep.api.preparation.Preparation;

import java.util.Comparator;

import static org.junit.Assert.*;
import static org.talend.dataprep.util.SortAndOrderHelper.Order.ASC;
import static org.talend.dataprep.util.SortAndOrderHelper.Order.DESC;
import static org.talend.dataprep.util.SortAndOrderHelper.Sort.NAME;

public class SortAndOrderHelperTest {

    @Test
    public void getPreparationComparator_byName() throws Exception {
        assertTrue(getComparisonByNameAsc("aaa", "bbb", NAME, ASC) < 0);
        assertTrue(getComparisonByNameAsc("aaa", "bbb", NAME, DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byNameUppercaseShouldNotInfluence() throws Exception {
        assertTrue(getComparisonByNameAsc("AAA", "bbb", NAME, ASC) < 0);

        assertTrue(getComparisonByNameAsc("aaa", "BBB", NAME, ASC) < 0);

        assertTrue(getComparisonByNameAsc("AAA", "bbb", NAME, DESC) > 0);

        assertTrue(getComparisonByNameAsc("aaa", "BBB", NAME, DESC) > 0);
    }

    private int getComparisonByNameAsc(String aaa, String bbb, SortAndOrderHelper.Sort sortField, SortAndOrderHelper.Order sortOrder) {
        Preparation firstNamedPrep = new Preparation();
        firstNamedPrep.setName(aaa);
        Preparation secondNamedPrep = new Preparation();
        secondNamedPrep.setName(bbb);

        // when
        Comparator<Preparation> preparationComparator =
                SortAndOrderHelper.getPreparationComparator(sortField.name(), sortOrder.name());

        // then
        assertNotNull(preparationComparator);
        return preparationComparator.compare(firstNamedPrep, secondNamedPrep);
    }

}
