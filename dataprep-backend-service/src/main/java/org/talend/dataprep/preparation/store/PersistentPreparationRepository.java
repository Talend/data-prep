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
import java.util.stream.Stream;

import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.conversions.BeanConversionService;
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

    public PersistentPreparationRepository(PreparationRepository delegate, BeanConversionService beanConversionService) {
        this.delegate = delegate;
        this.beanConversionService = beanConversionService;
    }

    private static Class<? extends Identifiable> selectPersistentClass(Class<? extends Identifiable> identifiableClass) {
        if (Preparation.class.isAssignableFrom(identifiableClass)) {
            return PersistentPreparation.class;
        } else if (Step.class.isAssignableFrom(identifiableClass)) {
            return PersistentStep.class;
        } else {
            // No need for specific persistent class.
            return identifiableClass;
        }
    }

    @Override
    public <T extends Identifiable> boolean exist(Class<T> clazz, Expression expression) {
        final Class<? extends Identifiable> targetClass = selectPersistentClass(clazz);
        return delegate.exist(targetClass, expression);
    }

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz) {
        final Class<T> persistentClass = (Class<T>) selectPersistentClass(clazz);
        return Stream.concat(delegate.list(persistentClass).map(i -> beanConversionService.convert(i, clazz)), getRootElement(persistentClass, clazz));
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

    @Override
    public <T extends Identifiable> Stream<T> list(Class<T> clazz, Expression expression) {
        final Class<T> persistentClass = (Class<T>) selectPersistentClass(clazz);
        return Stream.concat(delegate.list(persistentClass, expression).map(i -> beanConversionService.convert(i, clazz)), getRootElement(clazz, clazz));
    }

    @Override
    public void add(Identifiable object) {
        final Collection<Identifiable> identifiableList = PreparationUtils.scatter(object);
        for (Identifiable identifiable : identifiableList) {
            if (!(Step.ROOT_STEP.equals(identifiable) || PreparationActions.ROOT_ACTIONS.equals(identifiable))) {
                final Class<? extends Identifiable> targetClass = selectPersistentClass(identifiable.getClass());
                final Identifiable storedIdentifiable = beanConversionService.convert(identifiable, targetClass);
                delegate.add(storedIdentifiable);
            }
        }
    }

    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {
        final Class<T> targetClass = (Class<T>) selectPersistentClass(clazz);
        Object beanToConvert;
        if (clazz.equals(Step.class) && Step.ROOT_STEP.getId().equals(id)) {
            return (T) Step.ROOT_STEP;
        } else if (clazz.equals(PreparationActions.class) && PreparationActions.ROOT_ACTIONS.getId().equals(id)) {
            return (T) PreparationActions.ROOT_ACTIONS;
        } else {
            beanToConvert = delegate.get(id, targetClass);
        }
        return beanConversionService.convert(beanToConvert, clazz);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void remove(Identifiable object) {
        final Class<? extends Identifiable> targetClass = selectPersistentClass(object.getClass());
        delegate.remove(beanConversionService.convert(object, targetClass));
    }
}
