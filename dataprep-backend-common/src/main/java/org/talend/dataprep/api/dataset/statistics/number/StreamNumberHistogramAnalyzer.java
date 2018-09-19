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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.util.NumericHelper;
import org.talend.dataquality.common.inference.ResizableList;
import org.talend.dataquality.statistics.numeric.NumericalStatisticsAnalyzer;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dataquality.statistics.type.TypeInferenceUtils;

/**
 * Number histogram analyzer. It processes all the records and compute the statistics for each.
 */
public class StreamNumberHistogramAnalyzer extends NumericalStatisticsAnalyzer<StreamNumberHistogramStatistics> {

    private static final long serialVersionUID = -3756520692420812485L;

    private static final Logger LOGGER = getLogger(StreamNumberHistogramAnalyzer.class);

    private final ResizableList<StreamNumberHistogramStatistics> stats =
            new ResizableList<>(StreamNumberHistogramStatistics.class);

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
            throw new IllegalArgumentException(
                    "Each column of the record should be declared a DataType.Type corresponding! \n" + types.length
                            + " type(s) declared in this histogram analyzer but " + record.length
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
            // FixMe : fix this pb by https://jira.talendforge.org/browse/TDP-4684
            if (NumericHelper.isBigDecimal(value)) {
                try {
                    stats.get(index).add(BigDecimalParser.toBigDecimal(value).doubleValue());
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    LOGGER.debug("Unable to calculate action on {} due to the following exception {}.", value, e);
                } catch (Exception e) {
                    LOGGER.debug("Unable to calculate action on {} due to an unknown exception {}.", value, e);
                }
            }
        }
        return true;
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
