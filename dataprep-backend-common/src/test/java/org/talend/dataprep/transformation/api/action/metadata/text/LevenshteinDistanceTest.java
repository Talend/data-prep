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
package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for LevenshteinDistance action. Creates one consumer, and test it.
 *
 * @see LevenshteinDistance
 */
public class LevenshteinDistanceTest extends AbstractMetadataBaseTest {

    /**
     * The action to test.
     */
    @Autowired
    private LevenshteinDistance action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(LevenshteinDistanceTest.class.getResourceAsStream("levenshteinDistance.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_be_true_as_less_than_distance() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(LevenshteinDistance.VALUE_PARAMETER, "pale zle");
        parameters.put(LevenshteinDistance.DISTANCE_PARAMETER, "5");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "true"));

    }

    @Test
    public void should_be_true_as_equals_distance() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(LevenshteinDistance.VALUE_PARAMETER, "zale zne");
        parameters.put(LevenshteinDistance.DISTANCE_PARAMETER, "3");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "pale ale"), //
                        MapEntry.entry("0001", "true"));

    }

    @Test
    public void should_be_false_as_not_less_equals_than_distance() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "pale ale");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(LevenshteinDistance.VALUE_PARAMETER, "zale zSQ");
        parameters.put(LevenshteinDistance.DISTANCE_PARAMETER, "3");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
            .hasSize(2) //
            .containsExactly(MapEntry.entry("0000", "pale ale"), //
                             MapEntry.entry("0001", "false"));

    }

}
