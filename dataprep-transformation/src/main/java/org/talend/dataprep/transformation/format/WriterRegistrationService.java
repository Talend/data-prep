//  ============================================================================
//
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

package org.talend.dataprep.transformation.format;

import java.io.OutputStream;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

import static org.talend.dataprep.exception.error.TransformationErrorCodes.UNABLE_TO_USE_EXPORT;

/**
 * Service in charge of writers.
 */
@Service
public class WriterRegistrationService {

    /** Spring application context needed to get writer instance. */
    @Autowired
    private ApplicationContext context;

    @Autowired
    private FormatRegistrationService formatRegistrationService;

    /**
     * Return a TransformWriter that match the given format.
     *
     * @param format the wanted format id.
     * @param output Where the writer should write.
     * @param parameters Optional writer parameters. @return the TransformWriter that match the given format.
     */
    public TransformerWriter getWriter(String format, OutputStream output, Map<String, String> parameters) {
        try {
            // Sanity check -> ensures format is actually enabled before using it.
            final ExportFormat formatByName = formatRegistrationService.getByName(format);
            if (formatByName == null || !formatByName.isEnabled()) {
                throw new TDPException(UNABLE_TO_USE_EXPORT, ExceptionContext.build().put("format", format));
            }
            if (parameters.isEmpty()) {
                return (TransformerWriter) context.getBean("writer#" + format, output);
            } else {
                return (TransformerWriter) context.getBean("writer#" + format, output, parameters);
            }
        } catch (BeansException be) {
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED, be);
        }
    }

}
