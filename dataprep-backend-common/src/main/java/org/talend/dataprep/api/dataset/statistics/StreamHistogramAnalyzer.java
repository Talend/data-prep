// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.api.dataset.statistics;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.talend.dataquality.statistics.numeric.NumericalStatisticsAnalyzer;
import org.talend.dataquality.statistics.numeric.histogram.HistogramParameter;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.ResizableList;
import org.talend.datascience.common.inference.type.DataType.Type;
import org.talend.datascience.common.inference.type.TypeInferenceUtils;

/**
 */
public class StreamHistogramAnalyzer extends NumericalStatisticsAnalyzer<StreamHistogramStatistics> {

    private static final long serialVersionUID = -3756520692420812485L;

    private ResizableList<StreamHistogramStatistics> stats = new ResizableList<>(StreamHistogramStatistics.class);

    /**
     *
     * @param types data types
     * @param histogramParameter Histogram analyzer's parameter
     */
    public StreamHistogramAnalyzer(Type[] types, HistogramParameter histogramParameter) {
        super(types);
        if (histogramParameter == null) {
            throw new IllegalArgumentException("Histogram analyzer's parameter should is null.");
        }
    }

    @Override
    public boolean analyze(String... record) {
        Type[] types = getTypes();

        if (record.length != types.length)
            throw new IllegalArgumentException("Each column of the record should be declared a DataType.Type corresponding! \n"
                    + types.length + " type(s) declared in this histogram analyzer but " + record.length
                    + " column(s) was found in this record. \n"
                    + "Using method: setTypes(DataType.Type[] types) to set the types. ");

        if (stats.resize(record.length)) {
            for (StreamHistogramStatistics stat : stats) {
                // Set column parameters to histogram statistics.
                stat.setNumberOfBins(32);
            }
        }

        for (int id : this.getStatColIdx()) { // analysis each numerical column in the record
            if (!TypeInferenceUtils.isValid(types[id], record[id])) {
                continue;
            }
            analyzerHistogram(id, record);
        }
        return true;
    }

    private void analyzerHistogram(int index, String... record) {
        StreamHistogramStatistics histStats = stats.get(index);
        histStats.add(Double.valueOf(record[index]));
    }

    @Override
    public Analyzer<StreamHistogramStatistics> merge(Analyzer<StreamHistogramStatistics> another) {
        throw new NotImplementedException();
    }

    @Override
    public void end() {
    }

    @Override
    public List<StreamHistogramStatistics> getResult() {
        return stats;
    }

}
