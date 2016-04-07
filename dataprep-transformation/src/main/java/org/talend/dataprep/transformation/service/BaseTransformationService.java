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

package org.talend.dataprep.transformation.service;

import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.FormatRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class used to share code across all TransformationService implementation.
 */
public abstract class BaseTransformationService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BaseTransformationService.class);

    /** The transformer factory. */
    @Autowired
    private TransformerFactory factory;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** Preparation service url. */
    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    /** DataSet service url. */
    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    /** Http client used to retrieve datasets or preparations. */
    @Autowired
    protected HttpClient httpClient;

    /** The dataprep ready to use jackson object mapper. */
    @Autowired
    protected ObjectMapper mapper;

    /** Spring application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Transformation business logic.
     *
     * @param preparationId the preparation id to apply.
     * @param dataSet the dataset to transform.
     * @param response where to write the transformation.
     * @param formatName the wanted export format.
     * @param stepId the step id of the preparation to apply.
     * @param exportName the export name.
     * @param arguments the optional transformation argument (e.g. csv separator...)
     */
    protected void internalTransform(String preparationId, DataSet dataSet, OutputStream response, String formatName,
            String stepId, String exportName, Map<String, String> arguments) {

        final ExportFormat format = getFormat(formatName);

        // get the actions to apply (no preparation ==> dataset export ==> no actions)
        String actions;
        if (StringUtils.isBlank(preparationId)) {
            actions = "{\"actions\": []}";
        } else {
            final PreparationGetActions getActionsCommand = applicationContext.getBean(PreparationGetActions.class, preparationId, stepId);
            try {
                actions = "{\"actions\": "+ IOUtils.toString(getActionsCommand.execute()) + '}';
            } catch (IOException e) {
                final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", stepId);
                throw new TDPException(UNABLE_TO_READ_PREPARATION, e, context);
            }
        }

        setExportHeaders(exportName, format);

        Configuration configuration = Configuration.builder() //
                .format(format.getName()) //
                .args(arguments) //
                .output(response) //
                .actions(actions) //
                .build();

        factory.get(configuration).transform(dataSet, configuration);

    }

    /**
     * Set the content-type header for export.
     *
     * @param exportName the name of the export.
     * @param format the format to use.
     */
    protected void setExportHeaders(String exportName, ExportFormat format) {
        HttpResponseContext.contentType(format.getMimeType());
        HttpResponseContext.header("Content-Disposition", "attachment; filename=\"" + exportName + format.getExtension() + "\"");
    }

    /**
     * Return the format that matches the given name or throw an error if the format is unknown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    protected ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName);
        if (format == null) {
            LOG.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }

    /**
     * Filter export parameters so that only relevant parameters are left.
     *
     * @param optionalParams the raw export parameters.
     * @return the filtered export parameters.
     */
    protected Map<String, String> filterRawExportParams(Map<String, String> optionalParams) {
        return optionalParams.entrySet().stream()
                .filter(e -> !StringUtils.equals(e.getKey(), "fileName"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
