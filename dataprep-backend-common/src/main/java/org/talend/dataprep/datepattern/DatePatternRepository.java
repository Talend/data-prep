//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.datepattern;

import java.util.List;

/**
 * {@link DatePattern} management
 */
public interface DatePatternRepository {

    /**
     * @return a {@link Iterable} of all available {@link DatePattern}
     */
    List<DatePattern> all();

    /**
     * @param datePattern the {@link DatePattern} to add
     */
    void add(DatePattern datePattern);

    /**
     * @param datePattern the {@link DatePattern} to remove
     */
    void remove(DatePattern datePattern);

    /**
     * remove all datepattern
     */
    void clear();

}
