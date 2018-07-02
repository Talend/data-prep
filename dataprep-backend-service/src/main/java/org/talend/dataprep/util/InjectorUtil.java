//  ============================================================================
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

package org.talend.dataprep.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class InjectorUtil {

    @Autowired
    private ActionRegistry registry;


    public PreparationDetailsDTO injectPreparationDetails(List<Action> actions, PreparationDetailsDTO details) {
        // Append actions and action forms
        details.setActions(actions);
        final AtomicBoolean allowDistributedRun = new AtomicBoolean();
        final List<ActionForm> metadata = actions.stream() //
                .map(a -> registry.get(a.getName())) //
                .peek(a -> {
                    if (allowDistributedRun.get()) {
                        allowDistributedRun.set(a.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
                    }
                }) //
                .map(a -> a.getActionForm(LocaleContextHolder.getLocale(), Locale.US)) //
                .collect(Collectors.toList());
        details.setMetadata(metadata);

        // Flag for allow distributed run (based on metadata).
        details.setAllowDistributedRun(allowDistributedRun.get());

        return details;
    }
}
