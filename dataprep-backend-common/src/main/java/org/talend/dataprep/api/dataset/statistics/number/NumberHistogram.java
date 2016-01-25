package org.talend.dataprep.api.dataset.statistics.number;

import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;

import java.util.ArrayList;
import java.util.List;

public class NumberHistogram implements Histogram {
    private static final long serialVersionUID = 1L;

    public static final String TYPE = "number";

    private final List<HistogramRange> items = new ArrayList<>();

    @Override
    public List<HistogramRange> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NumberHistogram))
            return false;

        NumberHistogram that = (NumberHistogram) o;

        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
