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

package org.talend.dataprep.transformation.api.transformer.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PreviewConfiguration extends Configuration {

    private final String previewActions;

    /** Indexes of rows (used in diff). */
    private final List<Long> indexes;

    protected PreviewConfiguration(Configuration configuration, String previewActions, List<Long> indexes) {
        super(configuration.output(), configuration.getFilter(), configuration.getOutFilter(), configuration.getMonitor(), configuration.getSourceType(), configuration.formatId(), configuration.getActions(), configuration.getArguments(),
                configuration.getPreparation(), configuration.stepId(), false, false, configuration.volume(), null);
        this.previewActions = previewActions;
        this.indexes = indexes;
    }

    public static Builder preview() {
        return new Builder();
    }

    public List<Long> getIndexes() {
        return indexes;
    }

    public String getReferenceActions() {
        return super.getActions();
    }

    public String getPreviewActions() {
        return previewActions;
    }

    /**
     * Builder pattern used to simplify code writing.
     */
    public static class Builder {

        /** Indexes of rows. */
        private List<Long> indexes;

        private String previewActions;

        private Configuration reference;

        private List<Long> parseIndexes(final String indexes) {
            if (indexes == null) {
                return null;
            }
            try {
                final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
                final JsonNode json = mapper.readTree(indexes);

                final List<Long> result = new ArrayList<>(json.size());
                for (JsonNode index : json) {
                    result.add(index.longValue());
                }
                return result;
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_ACTIONS, e);
            }
        }

        public Builder withIndexes(final String indexes) {
            this.indexes = parseIndexes(indexes);
            return this;
        }

        public Builder withActions(final String previewActions) {
            this.previewActions = previewActions;
            return this;
        }

        public Builder fromReference(final Configuration reference) {
            this.reference = reference;
            return this;
        }

        public PreviewConfiguration build() {
            return new PreviewConfiguration(reference, previewActions, indexes);
        }

    }
}
