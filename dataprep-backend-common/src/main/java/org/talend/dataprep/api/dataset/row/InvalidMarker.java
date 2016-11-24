package org.talend.dataprep.api.dataset.row;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;

public class InvalidMarker implements Function<DataSetRow, DataSetRow> {

    /**
     * The invalid values number from the beginning of the records
     */
    private final long[] invalidCount;

    /**
     * The invalid analyzer
     */
    private final Analyzer<Analyzers.Result> analyzer;

    /**
     * The columns metadata
     */
    private final List<ColumnMetadata> columns;

    public InvalidMarker(List<ColumnMetadata> columns, Analyzer<Analyzers.Result> analyzer) {
        this.columns = columns;
        this.invalidCount = new long[columns.size()];
        this.analyzer = analyzer;
    }

    @Override
    public DataSetRow apply(DataSetRow dataSetRow) {
        // get the analyze from beginning
        final String[] values = dataSetRow.filter(columns).order(columns).toArray(DataSetRow.SKIP_TDP_ID);
        analyzer.analyze(values);
        final List<Analyzers.Result> columnsAnalysis = analyzer.getResult();

        // we will mark the columns that has a new invalid
        final BitSet invalidBitSet = new BitSet(values.length);

        // update the invalid count for each columns and mark those that have new invalids
        for (int i = 0; i < columnsAnalysis.size(); i++) {
            final Analyzers.Result columnAnalysis = columnsAnalysis.get(i);
            if (columnAnalysis.exist(ValueQualityStatistics.class)) {
                final long newInvalidCount = columnAnalysis.get(ValueQualityStatistics.class).getInvalidCount();
                if (newInvalidCount > invalidCount[i]) {
                    invalidBitSet.set(i);
                    invalidCount[i] = newInvalidCount;
                }
            }
        }

        // Set invalid / unknown status for columns
        for (int i = 0; i < columns.size(); i++) {
            final String columnId = columns.get(i).getId();
            if (invalidBitSet.get(i)) {
                dataSetRow.setInvalid(columnId);
            } else {
                dataSetRow.unsetInvalid(columnId);
            }
        }

        return dataSetRow;
    }
}
