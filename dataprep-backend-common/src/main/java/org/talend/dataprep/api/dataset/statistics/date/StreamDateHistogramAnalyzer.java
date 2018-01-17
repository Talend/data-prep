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

package org.talend.dataprep.api.dataset.statistics.date;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.transformation.actions.date.DateParser;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.common.inference.ResizableList;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dataquality.statistics.type.TypeInferenceUtils;

/**
 * Date histogram analyzer
 */
public class StreamDateHistogramAnalyzer implements Analyzer<StreamDateHistogramStatistics> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDateHistogramAnalyzer.class);

    /**
     * List of statistics (one for each column)
     */
    private final ResizableList<StreamDateHistogramStatistics> stats = new ResizableList<>(StreamDateHistogramStatistics.class);

    /**
     * The columns types
     */
    private final DataTypeEnum[] types;

    /**
     * A date parser, based on columns and analyzer
     */
    private final DateParser dateParser;

    /**
     * The columns metadata
     */
    private final List<ColumnMetadata> columns;

    /**
     * Constructor
     *
     * @param columns The columns metadata
     * @param types The columns data types
     * @param dateParser A date parser based on column metadata and DQ analyzer
     */
    public StreamDateHistogramAnalyzer(List<ColumnMetadata> columns, final DataTypeEnum[] types, final DateParser dateParser) {
        this.columns = columns;
        this.types = types;
        this.dateParser = dateParser;
    }

    @Override
    public boolean analyze(String... record) {
        if (record.length != types.length) {
            throw new IllegalArgumentException("Each column of the record should be declared a DataType.Type corresponding! \n"
                    + types.length + " type(s) declared in this histogram analyzer but " + record.length
                    + " column(s) was found in this record. \n"
                    + "Using method: setTypes(DataType.Type[] types) to set the types. ");
        }

        stats.resize(record.length);

        for (int index = 0; index < types.length; ++index) {
            final DataTypeEnum type = this.types[index];
            final ColumnMetadata column = this.columns.get(index);
            final String value = record[index];
            if (type == DataTypeEnum.DATE) {
                final String mostUsedDatePattern = RowMetadataUtils.getMostUsedDatePattern(column);
                if (!TypeInferenceUtils.isDate(value, Collections.singletonList(mostUsedDatePattern))) {
                    LOGGER.trace("Skip date value '{}' (not valid date)", value);
                    continue;
                }
                try {
                    final LocalDateTime adaptedValue = dateParser.parse(value, column);
                    stats.get(index).add(adaptedValue);
                } catch (DateTimeException e) {
                    // just skip this value
                    LOGGER.debug("Unable to process date value '{}'", value, e);
                }
            }
        }

        return true;
    }

    @Override
    public Analyzer<StreamDateHistogramStatistics> merge(Analyzer<StreamDateHistogramStatistics> another) {
        throw new NotImplementedException();
    }

    @Override
    public void end() {
        // Nothing to do
    }

    @Override
    public List<StreamDateHistogramStatistics> getResult() {
        return stats;
    }

    @Override
    public void init() {
        // Nothing to do
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }
}
