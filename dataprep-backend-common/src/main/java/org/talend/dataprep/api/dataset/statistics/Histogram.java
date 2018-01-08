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

package org.talend.dataprep.api.dataset.statistics;

import java.io.Serializable;
import java.util.List;

import org.talend.dataprep.api.dataset.statistics.date.DateHistogram;
import org.talend.dataprep.api.dataset.statistics.number.NumberHistogram;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = NumberHistogram.class, name = NumberHistogram.TYPE),
        @JsonSubTypes.Type(value = DateHistogram.class, name = DateHistogram.TYPE) })
public interface Histogram extends Serializable {

    List<HistogramRange> getItems();
}
