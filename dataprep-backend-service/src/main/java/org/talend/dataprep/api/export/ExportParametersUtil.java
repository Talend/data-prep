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

package org.talend.dataprep.api.export;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExportParametersUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportParametersUtil.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ObjectMapper mapper;

    /**
     * Return a copy of the export parameter with all clean value for a preparation (good step Id and good datasetId)
     *
     * @param exportParam incoming export param
     * @return a full value copy of the export parameter
     */
    public ExportParameters populateFromPreparationExportParameter(ExportParameters exportParam) throws IOException {
        ExportParameters result = new ExportParameters();
        result.setPreparationId(exportParam.getPreparationId());
        result.setExportType(exportParam.getExportType());
        result.setFrom(exportParam.getFrom());
        result.setContent(exportParam.getContent());
        result.setArguments(exportParam.getArguments());
        result.setExportName(exportParam.getExportName());
        result.setDatasetId(exportParam.getDatasetId());
        result.setFilter(exportParam.getFilter());

        // we deal with a preparation export parameter. We need to populate stepId and datasetId
        if(StringUtils.isNotEmpty(exportParam.getPreparationId())){
            Preparation prep = getPreparation(exportParam.getPreparationId(), exportParam.getStepId());
            result.setStepId(getCleanStepId(prep, exportParam.getStepId()));
            if(exportParam.getFrom() != ExportParameters.SourceType.FILTER){
                result.setDatasetId(prep.getDataSetId());
            }
        } else{
            // it'w a dataset export parameter. We need to switch stepId to empty
            result.setStepId("");
        }

        return result;
    }

    /**
     * @param preparationId the wanted preparation id.
     * @param stepId the preparation step (might be different from head's to navigate through versions).
     * @return the preparation out of its id.
     */
    public PreparationMessage getPreparation(String preparationId, String stepId) {
        if ("origin".equals(stepId)) {
            stepId = Step.ROOT_STEP.id();
        }
        final PreparationDetailsGet preparationDetailsGet = applicationContext.getBean(PreparationDetailsGet.class, preparationId, stepId);
        try (InputStream details = preparationDetailsGet.execute()) {
            return mapper.readerFor(PreparationMessage.class).readValue(details);
        } catch (Exception e) {
            LOGGER.error("Unable to read preparation {}", preparationId, e);
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }
    }

    /**
     * Return the real step id in case of "head" or empty
     *
     * @param preparation The preparation
     * @param stepId The step id
     */
    public String getCleanStepId(final Preparation preparation, final String stepId) {
        if (StringUtils.equals("head", stepId) || StringUtils.isEmpty(stepId)) {
            return preparation.getSteps().get(preparation.getSteps().size() - 1).id();
        }
        return stepId;
    }
}
