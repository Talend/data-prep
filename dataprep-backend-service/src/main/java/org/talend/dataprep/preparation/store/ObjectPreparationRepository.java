package org.talend.dataprep.preparation.store;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.filter.ObjectPredicateVisitor;
import org.talend.tql.parser.Tql;

public abstract class ObjectPreparationRepository implements PreparationRepository {

    protected abstract <T extends Identifiable> Stream<T> source(Class<T> clazz);

    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {
        final Optional<T> match = source(clazz).filter(i -> ObjectUtils.equals(i.getId(), id)).findAny();
        return match.isPresent() ? match.get() : null;
    }

    @Override
    public <T extends Identifiable> boolean exist(Class<T> clazz, String filter) {
        final Predicate<Object> accept = (Predicate<Object>) Tql.parse(filter)
                .accept(new ObjectPredicateVisitor(clazz));
        return source(clazz).filter(accept).findAny().isPresent();
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz) {
        return source(clazz);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz, String filter) {
        final Predicate<Object> accept = (Predicate<Object>) Tql.parse(filter)
                .accept(new ObjectPredicateVisitor(clazz));
        return source(clazz).filter(accept);
    }

}
