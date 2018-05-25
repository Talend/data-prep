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

package org.talend.dataprep.preparation.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.tql.api.TqlBuilder;
import org.talend.tql.model.Expression;

@Component
public class PreparationUpdateListener {

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired
    private PreparationRepository preparationRepository;

    @EventListener
    public void onUpdate(DatasetUpdatedEvent event) {
        final Expression filter = TqlBuilder.eq("dataSetId", event.getSource().getId());
        preparationRepository
                .list(Preparation.class, filter) //
                .forEach(preparation -> {
                    // Reset preparation row metadata.
                    preparation.setRowMetadata(event.getSource().getRowMetadata());
                    preparationRepository.add(preparation);

                    // Reset step row metadata in preparation's steps.
                    preparationUtils
                            .listSteps(preparation.getHeadId(), preparationRepository) //
                            .stream() //
                            .filter(s -> !Step.ROOT_STEP.id().equals(s.id())) //
                            .filter(s -> s.getRowMetadata() != null) //
                            .forEach(s -> {
                                final Expression expression = TqlBuilder.eq("id", s.getRowMetadata());
                                preparationRepository.remove(StepRowMetadata.class, expression);
                            });
                });
    }
}
