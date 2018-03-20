// ============================================================================
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

package org.talend.dataprep.preparation.store;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.tql.bean.BeanPredicateVisitor;
import org.talend.tql.model.Expression;

public abstract class ObjectPreparationRepository implements PreparationRepository {

    protected abstract <T extends Identifiable> Stream<T> source(Class<T> clazz);

    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {
        final Optional<T> match = source(clazz).filter(i -> ObjectUtils.equals(i.getId(), id)).findAny();
        return match.orElse(null);
    }

    @Override
    public <T extends Identifiable> boolean exist(Class<T> clazz, Expression expression) {
        final Predicate<T> accept = expression.accept(new BeanPredicateVisitor<>(clazz));
        return source(clazz).anyMatch(accept);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz) {
        return source(clazz);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz, Expression expression) {
        final Predicate<T> accept = expression.accept(new BeanPredicateVisitor<>(clazz));
        return source(clazz).filter(accept);
    }

}
