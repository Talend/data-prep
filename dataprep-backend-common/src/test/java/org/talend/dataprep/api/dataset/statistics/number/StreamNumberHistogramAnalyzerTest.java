// ============================================================================
//
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

package org.talend.dataprep.api.dataset.statistics.number;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.talend.dataquality.statistics.type.DataTypeEnum;

public class StreamNumberHistogramAnalyzerTest {

    @Test
    public void shouldComputeWithValidPercentageNumber() {

        // given
        final DataTypeEnum[] types =
                { DataTypeEnum.DOUBLE, DataTypeEnum.DOUBLE, DataTypeEnum.DOUBLE, DataTypeEnum.DOUBLE };
        final StreamNumberHistogramAnalyzer streamNumberHistogramAnalyzer = new StreamNumberHistogramAnalyzer(types);

        // when
        streamNumberHistogramAnalyzer.analyze("3.6E2", "1.7976931348623157E308", "test", "3.6 E20000000000");
        List<StreamNumberHistogramStatistics> listStreamNumberHistogramStatistics =
                streamNumberHistogramAnalyzer.getResult();

        // then
        assertThat(listStreamNumberHistogramStatistics.size(), is(4));

        StreamNumberHistogramStatistics validStreamNumberHistogramStatistics =
                listStreamNumberHistogramStatistics.get(0);
        assertThat(validStreamNumberHistogramStatistics.getNumberOfValues(), is(1L));
        assertThat(validStreamNumberHistogramStatistics.getMin(), is(360.0));
        assertThat(validStreamNumberHistogramStatistics.getMax(), is(360.0));
        assertThat(validStreamNumberHistogramStatistics.getMean(), is(360.0));

        StreamNumberHistogramStatistics bigValidStreamNumberHistogramStatistics =
                listStreamNumberHistogramStatistics.get(1);
        assertThat(bigValidStreamNumberHistogramStatistics.getNumberOfValues(), is(1L));
        assertThat(bigValidStreamNumberHistogramStatistics.getMin(), is(1.7976931348623157E308));
        assertThat(bigValidStreamNumberHistogramStatistics.getMax(), is(1.7976931348623157E308));
        assertThat(bigValidStreamNumberHistogramStatistics.getMean(), is(1.7976931348623157E308));

        StreamNumberHistogramStatistics textStreamNumberHistogramStatistics =
                listStreamNumberHistogramStatistics.get(2);
        assertThat(textStreamNumberHistogramStatistics.getNumberOfValues(), is(0L));
        assertThat(textStreamNumberHistogramStatistics.getMin(), is(0.0));
        assertThat(textStreamNumberHistogramStatistics.getMax(), is(0.0));
        assertThat(textStreamNumberHistogramStatistics.getMean(), is(0.0));

        StreamNumberHistogramStatistics inValidStreamNumberHistogramStatistics =
                listStreamNumberHistogramStatistics.get(3);
        assertThat(inValidStreamNumberHistogramStatistics.getNumberOfValues(), is(0L));
        // Note : getMean, getMin and getMax change Nan to 0
        // FixMe : fix this pb by https://jira.talendforge.org/browse/TDP-4684
        assertThat(inValidStreamNumberHistogramStatistics.getMin(), is(0.0));
        assertThat(inValidStreamNumberHistogramStatistics.getMax(), is(0.0));
        assertThat(inValidStreamNumberHistogramStatistics.getMean(), is(0.0));

    }
}
