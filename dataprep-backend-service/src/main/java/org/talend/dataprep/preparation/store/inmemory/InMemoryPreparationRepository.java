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

package org.talend.dataprep.preparation.store.inmemory;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.ObjectPreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * In memory Preparation repository.
 */
@Component
@ConditionalOnProperty(name = "preparation.store", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryPreparationRepository extends ObjectPreparationRepository {

    /**
     * The root step.
     */
    @Resource(name = "rootStep")
    private Step rootStep;

    /**
     * The default root content.
     */
    @Resource(name = "rootContent")
    private PreparationActions rootContent;

    /**
     * Map where preparations are stored.
     */
    private final Map<String, Identifiable> store = new HashMap<>();

    /**
     * Initialize root content.
     */
    @PostConstruct
    private void initRootContent() {
        add(rootContent);
        add(rootStep);
    }

    /**
     * @see PreparationRepository#add(Identifiable)
     */
    @Override
    public void add(Identifiable object) {
        store.put(object.id(), object);
    }

    @Override
    public <T extends Identifiable> Stream<T> source(Class<T> clazz) {
        return store.entrySet().stream() //
                .filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass())) //
                .map(entry -> (T) entry.getValue());
    }

    /**
     * @see PreparationRepository#get(String, Class)
     */
    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {
        if (id == null) {
            return null;
        }
        Identifiable value = store.get(id);
        if (value == null) {
            return null;
        }
        if (clazz == null) {
            return (T) value;
        } else if (clazz.isAssignableFrom(value.getClass())) {
            return clazz.cast(value);
        } else {
            return null;
        }
    }

    /**
     * @see PreparationRepository#clear()
     */
    @Override
    public void clear() {
        store.clear();
        add(rootContent);
        add(rootStep);
    }

    /**
     * @see PreparationRepository#remove
     */
    @Override
    public void remove(Identifiable object) {
        if (object == null) {
            return;
        }
        store.remove(object.id());
    }

}
