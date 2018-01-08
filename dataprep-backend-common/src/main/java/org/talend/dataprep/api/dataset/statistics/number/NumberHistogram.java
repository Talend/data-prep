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

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;

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
