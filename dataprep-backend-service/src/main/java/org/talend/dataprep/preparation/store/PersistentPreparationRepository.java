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

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.metrics.Timed;
import org.talend.tql.model.Expression;

/**
 * A {@link PreparationRepository} implementation that splits {@link Identifiable identifiable} into multiple ones for
 * persistent storage.
 *
 * @see PersistentIdentifiable
 * @see PreparationUtils#scatter(Identifiable)
 */
public class PersistentPreparationRepository implements PreparationRepository {

    private final BeanConversionService beanConversionService;

    private final PreparationRepository delegate;

    public PersistentPreparationRepository(PreparationRepository delegate,
            BeanConversionService beanConversionService) {
        this.delegate = delegate;
        this.beanConversionService = beanConversionService;
    }

    private static Class<? extends Identifiable>
            selectPersistentClass(Class<? extends Identifiable> identifiableClass) {
        if (Preparation.class.isAssignableFrom(identifiableClass)) {
            return PersistentPreparation.class;
        } else if (Step.class.isAssignableFrom(identifiableClass)) {
            return PersistentStep.class;
        } else {
            // No need for specific persistent class.
            return identifiableClass;
        }
    }

    private <T extends Identifiable> Stream<T> applyConversions(Supplier<Stream<T>> supplier, Class<T> clazz,
            Class<T> persistentClass) {
        Stream<T> delegateStream = supplier.get();
        if (!persistentClass.equals(clazz)) {
            delegateStream = delegateStream.map(i -> beanConversionService.convert(i, clazz));
        }
        return delegateStream;
    }

    @Timed
    @Override
    public <T extends Identifiable> boolean exist(Class<T> clazz, Expression expression) {
        final Class<? extends Identifiable> targetClass = selectPersistentClass(clazz);
        return delegate.exist(targetClass, expression);
    }

    @Timed
    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz) {
        final Class<T> persistentClass = (Class<T>) selectPersistentClass(clazz);
        Stream<T> delegateStream = applyConversions(() -> delegate.list(persistentClass), clazz, persistentClass);
        return Stream.concat(delegateStream, getRootElement(persistentClass, clazz));
    }

    private <T extends Identifiable> Stream<T> getRootElement(Class<T> clazz, Class<T> targetClass) {
        if (PersistentStep.class.isAssignableFrom(clazz)) {
            return Stream.of(beanConversionService.convert(Step.ROOT_STEP, targetClass));
        } else if (PreparationActions.class.isAssignableFrom(clazz)) {
            return Stream.of((T) PreparationActions.ROOT_ACTIONS);
        } else {
            return Stream.empty();
        }
    }

    @Timed
    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz, Expression expression) {
        final Class<T> persistentClass = (Class<T>) selectPersistentClass(clazz);
        Stream<T> delegateStream =
                applyConversions(() -> delegate.list(persistentClass, expression), clazz, persistentClass);
        return Stream.concat(delegateStream, getRootElement(clazz, clazz));
    }

    @Timed
    @Override
    public void add(Identifiable object) {
        final Class<? extends Identifiable> clazz = selectPersistentClass(object.getClass());
        if (!object.getClass().equals(clazz)) {
            final List<? extends Identifiable> objects = PreparationUtils
                    .scatter(object)
                    .stream() //
                    .filter(o -> !(Step.ROOT_STEP.equals(o) || PreparationActions.ROOT_ACTIONS.equals(o))) //
                    .map(identifiable -> {
                        final Class<? extends Identifiable> targetClass =
                                selectPersistentClass(identifiable.getClass());
                        return beanConversionService.convert(identifiable, targetClass);
                    }) //
                    .collect(Collectors.toList());
            delegate.add(objects);
        } else {
            delegate.add(object);
        }
    }

    @Timed
    @Override
    public void add(Collection<? extends Identifiable> objects) {
        for (Identifiable object : objects) {
            add(object);
        }
    }

    @Timed
    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {
        final Class<T> targetClass = (Class<T>) selectPersistentClass(clazz);
        Object beanToConvert;
        if (clazz.equals(Step.class) && Step.ROOT_STEP.getId().equals(id)) {
            return (T) Step.ROOT_STEP;
        } else if (clazz.equals(PersistentStep.class) && Step.ROOT_STEP.getId().equals(id)) {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setParentId(null);
            persistentStep.setId(Step.ROOT_STEP.id());
            persistentStep.setContent(PreparationActions.ROOT_ACTIONS.id());
            return (T) persistentStep;
        } else if (clazz.equals(PreparationActions.class) && PreparationActions.ROOT_ACTIONS.getId().equals(id)) {
            return (T) PreparationActions.ROOT_ACTIONS;
        } else {
            beanToConvert = delegate.get(id, targetClass);
        }
        return beanConversionService.convert(beanToConvert, clazz);
    }

    @Timed
    @Override
    public void clear() {
        delegate.clear();
    }

    @Timed
    @Override
    public void remove(Identifiable object) {
        final Class<? extends Identifiable> targetClass = selectPersistentClass(object.getClass());
        delegate.remove(beanConversionService.convert(object, targetClass));
    }

    @Timed
    @Override
    public <T extends Identifiable> void remove(Class<T> clazz, Expression filter) {
        list(clazz, filter).forEach(this::remove);
    }

    @Timed
    @Override
    public long count(Class<? extends Identifiable> clazz, Expression filter) {
        return delegate.count(selectPersistentClass(clazz), filter);
    }
}
