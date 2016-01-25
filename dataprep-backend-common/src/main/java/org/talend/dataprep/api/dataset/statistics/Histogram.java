package org.talend.dataprep.api.dataset.statistics;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.talend.dataprep.api.dataset.statistics.date.DateHistogram;
import org.talend.dataprep.api.dataset.statistics.number.NumberHistogram;

import java.io.Serializable;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumberHistogram.class, name = NumberHistogram.TYPE),
        @JsonSubTypes.Type(value = DateHistogram.class, name = DateHistogram.TYPE)
})
public interface Histogram extends Serializable {
    List<HistogramRange> getItems();
}
