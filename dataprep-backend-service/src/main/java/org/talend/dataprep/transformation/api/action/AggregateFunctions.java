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

package org.talend.dataprep.transformation.api.action;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A aggregator for multiple {@link BiConsumer consumers}. May be removed if a standard replacement is found. The
 * functions <b>must</b> have same return type as first parameter (but this is enforced by generic signature).
 * 
 * @param <T> the type of both first argument result of the function.
 * @param <U> the type of the second argument to the function.
 */
public class AggregateFunctions<T, U> implements BiFunction<T, U, T> {

    /** All aggregated functions */
    private final List<? extends BiFunction<T, U, T>> functions;

    /**
     * Create a {@link BiFunction} instance that groups all functions in parameters.
     *
     * @param actions {@link BiFunction Functions} to be aggregated.
     * @see #aggregate(List)
     */
    private AggregateFunctions(List<? extends BiFunction<T, U, T>> actions) {
        this.functions = actions;
    }

    /**
     * Aggregate all provided functions into one {@link BiConsumer} implementation.
     * 
     * @param functions {@link BiConsumer Consumers} to be aggregated.
     * @param <T> the type of both first argument and return type of the function.
     * @param <U> the type of the second argument to the function.
     * @return A unique {@link BiConsumer} that wraps all provided consumers in parameter.
     */
    public static <T, U> BiFunction<T, U, T> aggregate(List<? extends BiFunction<T, U, T>> functions) {
        return new AggregateFunctions<>(functions);
    }

    /**
     * @see BiFunction#apply(Object, Object)
     */
    @Override
    public T apply(T t, U u) {
        if (functions.isEmpty()) {
            return t;
        }
        final List<T> collect = functions.stream() //
                .filter(action -> action != null) //
                .map(action -> action.apply(t, u)) //
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return t;
        }
        return collect.get(collect.size() - 1); // Return the last transformed result
    }
}
