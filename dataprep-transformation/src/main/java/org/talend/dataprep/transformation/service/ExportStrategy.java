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

package org.talend.dataprep.transformation.service;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.lock.LockFactory;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.format.FormatRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportStrategy.class);

    @Autowired
    protected ApplicationContext applicationContext;

    /** The root step. */
    @Resource(name = "rootStep")
    protected Step rootStep;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected ContentCache contentCache;

    @Autowired
    protected FilterService filterService;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** The transformer factory. */
    @Autowired
    protected TransformerFactory factory;

    /** The lock factory. */
    @Autowired
    protected LockFactory lockFactory;

    /** The security proxy to use to get the dataset despite the roles/ownership. */
    @Autowired
    protected SecurityProxy securityProxy;

    /**
     * Return whether this export strategy is applicable to the parameters or not.
     * @param parameters Export parameters (can be null).
     * @return <code>true</code> if export strategy is applicable to parameters.
     */
    public abstract boolean accept(ExportParameters parameters);

    /**
     * Execute export strategy with given parameters. Callers are expected to ensure {@link #accept(ExportParameters)}
     * returns <code>true</code> before calling this method.
     * @param parameters Export parameters (can <b>not</b> be null).
     * @return A {@link StreamingResponseBody} that streams the export data to the provided output stream.
     */
    public abstract StreamingResponseBody execute(ExportParameters parameters);

    /**
     * Return the format that matches the given name or throw an error if the format is unknown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    protected ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName.toUpperCase());
        if (format == null) {
            LOGGER.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }

    /**
     * Return the real step id in case of "head" or empty
     * @param preparation The preparation
     * @param stepId The step id
     */
    protected String getCleanStepId(final Preparation preparation, final String stepId) {
        if (StringUtils.equals("head", stepId) || StringUtils.isEmpty(stepId)) {
            return preparation.getSteps().get(preparation.getSteps().size() - 1).id();
        }
        return stepId;
    }

    /**
     * Returns the actions for the preparation with <code>preparationId</code> at given <code>stepId</code>.
     *
     * @param preparationId The preparation id, if <code>null</code> or blank, returns <code>{actions: []}</code>
     * @param stepId A step id that must exist in given preparation id.
     * @return The actions that can be parsed by ActionParser.
     * @see org.talend.dataprep.transformation.api.action.ActionParser
     */
    protected String getActions(String preparationId, String stepId) {
        String actions;
        if (StringUtils.isBlank(preparationId)) {
            actions = "{\"actions\": []}";
        } else {
            final PreparationGetActions getActionsCommand = applicationContext.getBean(PreparationGetActions.class, preparationId,
                    stepId);
            try {
                actions = "{\"actions\": " + IOUtils.toString(getActionsCommand.execute()) + '}';
            } catch (IOException e) {
                final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", stepId);
                throw new TDPException(UNABLE_TO_READ_PREPARATION, e, context);
            }
        }
        return actions;
    }

    /**
     * Returns the actions for the preparation with <code>preparationId</code> between <code>startStepId</code> and
     * <code>endStepId</code>.
     *
     * @param preparationId The preparation id, if <code>null</code> or blank, returns <code>{actions: []}</code>
     * @param startStepId A step id that must exist in given preparation id.
     * @param endStepId A step id that must exist in given preparation id.
     * @return The actions that can be parsed by ActionParser.
     * @see org.talend.dataprep.transformation.api.action.ActionParser
     */
    protected String getActions(String preparationId, String startStepId, String endStepId) {
        if (rootStep.id().equals(startStepId)) {
            return getActions(preparationId, endStepId);
        }
        String actions;
        if (StringUtils.isBlank(preparationId)) {
            actions = "{\"actions\": []}";
        } else {
            try {
                final PreparationGetActions startStepActions = applicationContext.getBean(PreparationGetActions.class,
                        preparationId, startStepId);
                final PreparationGetActions endStepActions = applicationContext.getBean(PreparationGetActions.class,
                        preparationId, endStepId);
                final StringWriter actionsAsString = new StringWriter();
                final Action[] startActions = mapper.readValue(startStepActions.execute(), Action[].class);
                final Action[] endActions = mapper.readValue(endStepActions.execute(), Action[].class);
                if (endActions.length > startActions.length) {
                    final Action[] filteredActions = (Action[]) ArrayUtils.subarray(endActions, startActions.length,
                            endActions.length);
                    LOGGER.debug("Reduced actions list from {} to {} action(s)", endActions.length, filteredActions.length);
                    mapper.writeValue(actionsAsString, filteredActions);
                } else {
                    LOGGER.debug("Unable to reduce list of actions (has {})", endActions.length);
                    mapper.writeValue(actionsAsString, endActions);
                }

                return "{\"actions\": " + actionsAsString + '}';
            } catch (IOException e) {
                final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", endStepId);
                throw new TDPException(UNABLE_TO_READ_PREPARATION, e, context);
            }
        }
        return actions;
    }

    /**
     * @param preparationId the wanted preparation id.
     * @return the preparation out of its id.
     */
    protected PreparationMessage getPreparation(String preparationId) {
        final PreparationDetailsGet preparationDetailsGet = applicationContext.getBean(PreparationDetailsGet.class,
                preparationId);
        try (InputStream details = preparationDetailsGet.execute()) {
            return mapper.readerFor(PreparationMessage.class).readValue(details);
        } catch (Exception e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }

    }

}
