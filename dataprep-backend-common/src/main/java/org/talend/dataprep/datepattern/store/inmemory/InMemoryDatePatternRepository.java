// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.datepattern.store.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.datepattern.DatePatternRepository;

@Component("datePatternRepository#in-memory")
@ConditionalOnProperty(name = "datepattern.store", havingValue = "in-memory")
public class InMemoryDatePatternRepository implements DatePatternRepository {

    private Set<DatePattern> allDatePatterns = new CopyOnWriteArraySet<>();

    @Override
    public void add(DatePattern datePattern) {
        allDatePatterns.add(datePattern);
    }

    @Override
    public List<DatePattern> all() {
        return new ArrayList<>(allDatePatterns);
    }

    @Override
    public void remove(DatePattern datePattern) {
        allDatePatterns.remove(datePattern);
    }

    @Override
    public void clear() {
        allDatePatterns.clear();
    }
}
