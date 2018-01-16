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

package org.talend.dataprep.transformation.util;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommandUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtil.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ObjectMapper mapper;

    public DataSetGet getDataSet(String dataSetId) {
        return applicationContext.getBean(DataSetGet.class, dataSetId, false, true);
    }

    /**
     * @param preparationId the wanted preparation id.
     * @return the preparation command out of its id.
     */
    public PreparationDetailsGet getPreparationDetailsCommand(String preparationId) {
        return getPreparationDetailsCommand(preparationId, null);
    }

    /**
     * @param preparationId the wanted preparation id.
     * @param stepId the preparation step (might be different from head's to navigate through versions).
     * @return the preparation command out of its id.
     */
    public PreparationDetailsGet getPreparationDetailsCommand(String preparationId, String stepId) {
        if ("origin".equals(stepId)) {
            stepId = Step.ROOT_STEP.id();
        }
        return applicationContext.getBean(PreparationDetailsGet.class, preparationId, stepId);
    }

    public PreparationMessage getPreparation(String preparationId, String stepId) {
        final PreparationDetailsGet preparationDetailsGet = getPreparationDetailsCommand(preparationId, stepId);
        try (InputStream details = preparationDetailsGet.execute()) {
            return mapper.readerFor(PreparationMessage.class).readValue(details);
        } catch (Exception e) {
            LOGGER.error("Unable to read preparation {}", preparationId, e);
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }
    }

    public PreparationMessage getPreparation(String preparationId) {
        return this.getPreparation(preparationId, null);
    }
}
