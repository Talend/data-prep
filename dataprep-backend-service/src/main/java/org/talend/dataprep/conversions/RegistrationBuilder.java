/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.conversions;

import java.util.*;
import java.util.function.BiFunction;

public class RegistrationBuilder<T> {

    private final List<Class<?>> destinations = new ArrayList<>();

    private final Class<T> source;

    private final Map<Class<?>, BiFunction<T, Object, Object>> customs = new HashMap<>();

    RegistrationBuilder(Class<T> source) {
        this.source = source;
    }

    public RegistrationBuilder<T> toBeans(Class<?>... destinations) {
        Collections.addAll(this.destinations, destinations);
        return this;
    }

    public <U> RegistrationBuilder<T> using(Class<U> destination, BiFunction<T, U, U> custom) {
        customs.put(destination, (BiFunction<T, Object, Object>) custom);
        return this;
    }

    public Registration<T> build() {
        return new RegistrationImpl<>(source, destinations, customs);
    }

}
