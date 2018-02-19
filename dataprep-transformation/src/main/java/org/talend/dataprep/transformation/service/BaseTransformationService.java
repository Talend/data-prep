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

package org.talend.dataprep.transformation.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.format.FormatRegistrationService;
import org.talend.dataprep.transformation.service.export.SampleExportStrategy;
import org.talend.dataprep.util.OrderedBeans;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class used to share code across all TransformationService implementation.
 */
public abstract class BaseTransformationService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BaseTransformationService.class);

    /** The transformer factory. */
    @Autowired
    protected TransformerFactory factory;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** Preparation service url. */
    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    /** DataSet service url. */
    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    /** The dataprep ready to use jackson object mapper. */
    @Autowired
    protected ObjectMapper mapper;

    /** Spring application context. */
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected OrderedBeans<SampleExportStrategy> sampleExportStrategies;

    /**
     * Return the format that matches the given name or throw an error if the format is unknown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    protected ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName.toUpperCase());
        if (format == null) {
            LOG.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }

    StreamingResponseBody executeSampleExportStrategy(final ExportParameters parameters) {
        LOG.debug("Export for preparation #{}.", parameters.getPreparationId());
        // Full run execution (depends on the export parameters).
        try {
            final Optional<? extends ExportStrategy> electedStrategy = sampleExportStrategies //
                    .filter(exportStrategy -> exportStrategy.accept(parameters)) //
                    .findFirst();
            if (electedStrategy.isPresent()) {
                LOG.debug("Strategy for execution: {}", electedStrategy.get().getClass());
                return electedStrategy.get().execute(parameters);
            } else {
                throw new IllegalArgumentException("Not valid export parameters (no preparation id nor data set id).");
            }
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }
    }
}
