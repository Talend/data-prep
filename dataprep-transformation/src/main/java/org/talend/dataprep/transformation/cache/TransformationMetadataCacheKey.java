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

package org.talend.dataprep.transformation.cache;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Content cache key used to cache transformation.
 */
public class TransformationMetadataCacheKey implements ContentCacheKey {

    private static final String PREFIX = "transformation-metadata";

    private final String preparationId;

    private final String stepId;

    private ExportParameters.SourceType sourceType;

    private final String userId;

    TransformationMetadataCacheKey(final String preparationId, final String stepId, final ExportParameters.SourceType sourceType, final String userId) {
        if (StringUtils.equals("head", stepId)) {
            throw new IllegalArgumentException("'head' is not allowed as step id for cache key");
        }
        this.preparationId = preparationId;
        this.stepId = stepId;
        this.sourceType = sourceType;
        this.userId = userId;
    }

    @Override
    public String getKey() {
        return PREFIX + "_" + preparationId + "_" + stepId + "_" + sourceType + "_" + userId;
    }

    @Override
    public Predicate<String> getMatcher() {
        final String regex = PREFIX + '_'
                + (preparationId == null ? ".*" : preparationId) + "_"
                + (stepId == null ? ".*" : stepId) + "_"
                + (sourceType == null ? ".*" : sourceType) + "_"
                + (userId == null ? ".*" : userId) + "([.].*)?";
        final Pattern pattern = Pattern.compile(regex);
        return str -> pattern.matcher(str).matches();
    }

    @Override
    public String getPrefix() {
        StringBuilder prefix = new StringBuilder(PREFIX);
        if (preparationId != null) {
            prefix.append("_").append(preparationId);
        }
        return prefix.toString();
    }

    public String getPreparationId() {
        return preparationId;
    }

    public String getStepId() {
        return stepId;
    }

    public ExportParameters.SourceType getSourceType() {
        return sourceType;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "TransformationMetadataCacheKey{" + //
                "preparationId='" + preparationId + '\'' + //
                ", stepId='" + stepId + '\'' + //
                ", sourceType=" + sourceType + '\'' + //
                ", userId=" + userId + '\'' + //
                '}';
    }
}
