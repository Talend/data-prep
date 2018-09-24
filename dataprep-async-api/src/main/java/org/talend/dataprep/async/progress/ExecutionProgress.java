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

package org.talend.dataprep.async.progress;

/**
 * Asynchronous sampling progress information.
 * <p>
 * Asynchronously executed code may store :
 * </p>
 * <ul>
 * <li>processedRows: the number of rows processed so far</li>
 * <li>targetRows: the number of rows expected to be processed</li>
 * <li>percentage: the 0 to 1 float value that represents the advancement.</li>
 * </ul>
 */
public class ExecutionProgress {

    /** How many rows are target. */
    private Long targetRows;

    /** How many rows are processed so far. */
    private Long processedRows;

    /** The processing percentage. */
    private Float percentage;

    /**
     * Default empty constructor needed for the Json serialization.
     */
    public ExecutionProgress() {
    }

    public ExecutionProgress(long processedRows, long targetRows) {
        this.processedRows = processedRows;
        this.targetRows = targetRows;
        this.percentage = ((float) processedRows) / ((float) targetRows);
    }

    public ExecutionProgress(Float percentage) {
        this.percentage = percentage;
    }

    public Long getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(Long processedRows) {
        this.processedRows = processedRows;
    }

    public Long getTargetRows() {
        return targetRows;
    }

    public void setTargetRows(Long targetRows) {
        this.targetRows = targetRows;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "ExecutionProgress{" + //
                "targetRows=" + targetRows + //
                ", processedRows=" + processedRows + //
                ", percentage=" + percentage + //
                '}';
    }
}
