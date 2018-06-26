/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.transformation.service.export;

import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.service.ExportStrategy;

/**
 * Tagging interface for export strategies that can be used for sampling.
 */
public interface SampleExportStrategy extends ExportStrategy {

    /**
     * Execute export strategy with the given parameter and write the result to the cache. Callers are expected to ensure {@link #accept(ExportParameters)}
     * returns <code>true</code> before calling this method.
     *
     * @param parameters
     * @param key
     */
    void writeToCache(ExportParameters parameters, TransformationCacheKey key);
}
