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

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Content cache key used to cache transformation.
 */
public class TransformationCacheKey implements ContentCacheKey {

    private static final String PREFIX = "transformation";

    /** Format parameters (if any, if none, default to empty string) */
    private final String parameters;

    /** The source type */
    private final ExportParameters.SourceType sourceType;

    /** The User id */
    private final String userId;

    /** The dataset id. */
    private String datasetId;

    /** The preparation id. */
    private String preparationId;

    /** The optional step id. */
    private String stepId;

    /** The transformation format. */
    private String format;

    /** Transformation filter. */
    private String filter;

    /**
     * Create a cache key for transformation result content
     * @param preparationId The preparation id.
     * @param datasetId the dataset id.
     * @param format The output format.
     * @param stepId The step id.
     * @param parameters Additional parameters.
     * @param sourceType The source type.
     * @param userId The user id.
     */
    TransformationCacheKey(
            final String preparationId,
            final String datasetId,
            final String format,
            final String stepId,
            final String parameters,
            final ExportParameters.SourceType sourceType,
            final String userId, final String filter) {

        if (StringUtils.equals("head", stepId)) {
            throw new IllegalArgumentException("'head' is not allowed as step id for cache key");
        }

        this.preparationId = preparationId;
        this.datasetId = datasetId;
        this.format = format;
        this.stepId = stepId;
        this.parameters = parameters;
        this.sourceType = sourceType;
        this.userId = userId;
        this.filter = filter;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "TransformationCacheKey{"
                + "datasetId='" + datasetId  + '\''
                + ", preparationId='" + preparationId + '\''
                + ", format='" + format + '\''
                + ", stepId='" + stepId + '\''
                + ", parameters='" + parameters + '\''
                + ", sourceType='" + sourceType + '\''
                + ", userId='" + userId + '\''
                + ", filter='" + filter + '\''
                + '}';
    }

    /**
     * The key must be unique per content !
     *
     * @return the key for this cache content as a string.
     */
    @Override
    public String getKey() {
        return PREFIX + '_' + preparationId + "_" + datasetId + "_"
                + DigestUtils.sha1Hex(stepId + format + Objects.hash(parameters) + sourceType + userId + filter);
    }

    @Override
    public String getPrefix() {
        StringBuilder prefix = new StringBuilder(PREFIX);
        if (preparationId != null) {
            prefix.append("_").append(preparationId);
            if (datasetId != null) {
                prefix.append("_").append(datasetId);
            }
        }
        return prefix.toString();
    }

    @Override
    public Predicate<String> getMatcher() {
        // Build a regular expression using transformation and dataset ids.
        String regex = PREFIX + "_";
        if (preparationId == null) {
            regex += ".*";
        } else {
            regex += preparationId;
        }
        regex += "_";
        if (datasetId == null) {
            regex += ".*";
        } else {
            regex += datasetId;
        }
        regex += "_.*";

        // Build regular expression matcher
        final Pattern pattern = Pattern.compile(regex);
        return str -> pattern.matcher(str).matches();
    }
}
