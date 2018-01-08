//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.aggregation.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.talend.dataprep.transformation.aggregation.api.json.AggregationResultSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Models aggregation result : a map of working context indexed by key.
 */
@JsonSerialize(using = AggregationResultSerializer.class)
public class AggregationResult {

    /** The aggregation operator. */
    private Operator operator;

    /** Where all the results are stored. */
    private Map<String, WorkingContext> results;

    /**
     * Default constructor.
     * 
     * @param operator the aggregation operator.
     */
    public AggregationResult(Operator operator) {
        results = new HashMap<>();
        this.operator = operator;
    }

    /**
     * @param key the key to look for.
     * @return true if this result contains the given key.
     */
    public boolean contains(String key) {
        return results.containsKey(key);
    }

    /**
     * Put the given value at the given key.
     * 
     * @param key where to put the value.
     * @param value the value to put.
     */
    public void put(String key, WorkingContext value) {
        results.put(key, value);
    }

    /**
     * Return the value from the given key or null if the key is not found.
     * 
     * @param key the key to look for.
     * @return the current working context.
     */
    public WorkingContext get(String key) {
        return results.get(key);
    }

    /**
     * @return the Operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * @return the result entries.
     */
    public Set<Map.Entry<String, WorkingContext>> entries() {
        return results.entrySet();
    }

    /**
     * Removes an entry in aggregation result.
     * @param entry The entry to be removed
     */
    public void remove(String entry) {
        results.remove(entry);
    }
}
