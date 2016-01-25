package org.talend.dataprep.api.dataset.statistics.date;

import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;
import org.talend.dataprep.date.DateManipulator;

import java.util.ArrayList;
import java.util.List;

public class DateHistogram implements Histogram {
    private static final long serialVersionUID = 1L;

    public static final String TYPE = "date";

    private final List<HistogramRange> items = new ArrayList<>();
    private DateManipulator.Pace pace;

    @Override
    public List<HistogramRange> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DateHistogram))
            return false;

        DateHistogram that = (DateHistogram) o;

        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    public void setPace(DateManipulator.Pace pace) {
        this.pace = pace;
    }

    public DateManipulator.Pace getPace() {
        return pace;
    }
}
