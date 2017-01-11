// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides utility to parses actions and associated {@link RowMetadata}.
 */
public class PreparationParser {

    public static final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static ObjectMapper mapper = new ObjectMapper();

    private static void assertPreparation(Object preparation) {
        if (preparation == null) {
            throw new IllegalArgumentException("Preparation can not be null.");
        }
    }

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new MixedContentMapModule());
    }

    public static PreparationMessage parseCorePreparation(InputStream preparation) {
        assertPreparation(preparation);
        try {
            final PreparationMessage preparationMessage = mapper.reader(PreparationMessage.class).readValue(preparation);
            if (preparationMessage.getRowMetadata() == null) {
                preparationMessage.setRowMetadata(new RowMetadata());
            }
            return preparationMessage;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse preparation", e);
        }
    }

    public static StandalonePreparation parseExportableCorePreparation(InputStream inputStream) {
        try {
            return mapper.reader(StandalonePreparation.class).readValue(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create the provided exportable preparation into a function" + e);
        }
    }

    public static List<RunnableAction> ensureActionRowsExistence(List<Action> actions) {
        return actions.stream().map(a -> actionFactory.create(actionRegistry.get(a.getName()), a.getParameters()))
                .collect(Collectors.toList());
    }

}
