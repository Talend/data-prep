package org.talend.dataprep.preparation.store;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.filter.ObjectPredicateVisitor;
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
        final Predicate<Object> accept = (Predicate<Object>) expression.accept(new ObjectPredicateVisitor(clazz));
        return source(clazz).anyMatch(accept);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz) {
        return source(clazz);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz, Expression expression) {
        final Predicate<Object> accept = (Predicate<Object>) expression.accept(new ObjectPredicateVisitor(clazz));
        return source(clazz).filter(accept);
    }

}
