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

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.common.inference.ResizableList;
import org.talend.dataquality.statistics.numeric.NumericalStatisticsAnalyzer;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dataquality.statistics.type.TypeInferenceUtils;

/**
 * Number histogram analyzer. It processes all the records and compute the statistics for each.
 */
public class StreamNumberHistogramAnalyzer extends NumericalStatisticsAnalyzer<StreamNumberHistogramStatistics> {

    private static final long serialVersionUID = -3756520692420812485L;

    private final ResizableList<StreamNumberHistogramStatistics> stats = new ResizableList<>(
            StreamNumberHistogramStatistics.class);

    /**
     * Constructor
     *
     * @param types data types
     */
    public StreamNumberHistogramAnalyzer(DataTypeEnum[] types) {
        super(types);
    }

    @Override
    public boolean analyze(String... record) {
        DataTypeEnum[] types = getTypes();

        if (record.length != types.length)
            throw new IllegalArgumentException("Each column of the record should be declared a DataType.Type corresponding! \n"
                    + types.length + " type(s) declared in this histogram analyzer but " + record.length
                    + " column(s) was found in this record. \n"
                    + "Using method: setTypes(DataType.Type[] types) to set the types. ");

        if (stats.resize(record.length)) {
            for (StreamNumberHistogramStatistics stat : stats) {
                // Set column parameters to histogram statistics.
                stat.setNumberOfBins(32);
            }
        }

        for (int index : this.getStatColIdx()) { // analysis each numerical column in the record
            final String value = record[index];
            if (!TypeInferenceUtils.isValid(types[index], value)) {
                continue;
            }
            stats.get(index).add(BigDecimalParser.toBigDecimal(value).doubleValue());
        }
        return true;
    }

    @Override
    public Analyzer<StreamNumberHistogramStatistics> merge(Analyzer<StreamNumberHistogramStatistics> another) {
        throw new NotImplementedException();
    }

    @Override
    public void end() {
        // nothing to do here
    }

    @Override
    public List<StreamNumberHistogramStatistics> getResult() {
        return stats;
    }

}
