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

package org.talend.dataprep.helper.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload send for aggregate.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Aggregate {

    public String datadetId;

    public String preparationId;

    public String stepId;

    public List<AggregateOperation> operations;

    public List<String> groupBy;

    public void addGroupBy(String groupBy) {
        if (this.groupBy == null) {
            this.groupBy = new ArrayList<>();
        }
        this.groupBy.add(groupBy);
    }

    public void addOperation(AggregateOperation operation) {
        if (this.operations == null) {
            this.operations = new ArrayList<>();
        }
        this.operations.add(operation);
    }
}
