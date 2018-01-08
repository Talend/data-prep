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

package org.talend.dataprep.transformation.service.export;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.PreparationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

/**
 * A {@link BaseExportStrategy strategy} to reuse previous preparation export if available (if no previous content found
 * {@link #accept(ExportParameters)} returns <code>false</code>).
 */
@Component
public class CachedExportStrategy extends BaseSampleExportStrategy {

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Override
    public boolean accept(ExportParameters parameters) {
        if (parameters == null) {
            return false;
        }
        if (parameters.getContent() != null) {
            return false;
        }
        if (StringUtils.isEmpty(parameters.getPreparationId())) {
            return false;
        }
        try {
            final TransformationCacheKey contentKey = getCacheKey(parameters);
            return contentCache.has(contentKey);
        } catch (TDPException e) {
            if (e.getCode() == PreparationErrorCodes.UNABLE_TO_READ_PREPARATION) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public StreamingResponseBody execute(ExportParameters parameters) {
        final TransformationCacheKey contentKey = getCacheKey(parameters);
        ExportUtils.setExportHeaders(parameters.getExportName(), //
                parameters.getArguments().get(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING), //
                getFormat(parameters.getExportType()));
        return outputStream -> {
            try (InputStream cachedContent = contentCache.get(contentKey)) {
                IOUtils.copy(cachedContent, outputStream);
            }
        };
    }

    private TransformationCacheKey getCacheKey(ExportParameters parameters) {
        final PreparationMessage preparation = getPreparation(parameters.getPreparationId());
        return cacheKeyGenerator.generateContentKey(preparation.getDataSetId(), //
                parameters.getPreparationId(), //
                getCleanStepId(preparation, parameters.getStepId()), //
                parameters.getExportType(), //
                parameters.getFrom(), //
                parameters.getArguments(), //
                parameters.getFilter() //
        );
    }

}
