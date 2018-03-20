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

import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.format.JsonFormat;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

/**
 * Full configuration for a transformation.
 */
public class Configuration {

    private final Predicate<DataSetRow> filter;

    private final Function<RowMetadata, Predicate<DataSetRow>> outFilter;

    private final Supplier<Node> monitorSupplier;

    private final String stepId;

    /**
     * The format format {@link ExportFormat}
     */
    private final String format;

    /**
     * The format format {@link ExportParameters.SourceType}
     */
    private final ExportParameters.SourceType sourceType;

    /**
     * The actions in JSON string format
     */
    private final String actions;

    /**
     * The arguments for format
     */
    private final Map<String, String> arguments;

    /**
     * Where to write the transformed content.
     */
    private final OutputStream output;

    private final boolean allowMetadataChange;

    private final boolean globalStatistics;

    private final Volume dataVolume;

    private String preparationId;

    private PreparationMessage preparation;

    /** Limit the output number of rows. */
    private Long limit = null;

    /**
     * Constructor for the transformer configuration.
     */
    protected Configuration(final OutputStream output, //
                            final Predicate<DataSetRow> filter, //
                            final Function<RowMetadata, Predicate<DataSetRow>> outFilter, //
                            final Supplier<Node> monitorSupplier, //
                            final ExportParameters.SourceType sourceType, //
                            final String format, //
                            final String actions, //
                            final Map<String, String> arguments, //
                            final PreparationMessage preparation, //
                            final String stepId, //
                            boolean allowMetadataChange, //
                            boolean globalStatistics, //
            final Volume dataVolume, //
            final Long limit) {
        this.output = output;
        this.filter = filter;
        this.outFilter = outFilter;
        this.monitorSupplier = monitorSupplier;
        this.sourceType = sourceType;
        this.format = format;
        this.actions = actions;
        this.arguments = arguments;
        this.preparation = preparation;
        this.stepId = stepId;
        this.allowMetadataChange = allowMetadataChange;
        this.globalStatistics = globalStatistics;
        this.dataVolume = dataVolume;
        this.limit = limit;
    }

    /**
     * @return a TransformerConfiguration mapper.
     */
    public static Builder builder() {
        return new Builder();
    }

    public Supplier<Node> getMonitor() {
        return monitorSupplier;
    }

    public String stepId() {
        return stepId;
    }

    /**
     * @return The expected output {@link ExportFormat format} of the transformation.
     */
    public String formatId() {
        return format;
    }

    /**
     * @return The actions (as JSON string) to apply in transformation.
     */
    public String getActions() {
        return actions;
    }

    /**
     * @return Arguments for the {@link ExportFormat format type} (parameters depend on the format type).
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * @return the writer where to write the transform dataset.
     */
    public OutputStream output() {
        return output;
    }

    public Volume volume() {
        return dataVolume;
    }

    public boolean isAllowMetadataChange() {
        return allowMetadataChange;
    }

    public Predicate<DataSetRow> getFilter() {
        return filter;
    }

    public Function<RowMetadata, Predicate<DataSetRow>> getOutFilter() {
        return outFilter;
    }

    public boolean isGlobalStatistics() {
        return globalStatistics;
    }

    public String getPreparationId() {
        return preparation == null ? StringUtils.EMPTY : preparation.id();
    }

    public ExportParameters.SourceType getSourceType() {
        return sourceType;
    }

    public PreparationMessage getPreparation() {
        return preparation;
    }

    public Long getLimit() {
        return limit;
    }

    public enum Volume {
        LARGE,
        SMALL
    }

    /**
     * Builder pattern used to simplify code writing.
     */
    public static class Builder {

        /**
         * The format format {@link ExportFormat}
         */
        private String format = JsonFormat.JSON;

        /**
         * The format format {@link ExportFormat}
         */
        private ExportParameters.SourceType sourceType = ExportParameters.SourceType.HEAD;

        /**
         * The actions in JSON string format
         */
        private String actions = StringUtils.EMPTY;

        /**
         * The actions in Map
         */
        private Map<String, String> arguments = Collections.emptyMap();

        /**
         * Where to write the transformed content.
         */
        private OutputStream output;

        /**
         * Gives hint on the amount of data the transformer may expect
         */
        private Volume dataVolume = Volume.SMALL;

        private String stepId;

        private PreparationMessage preparation = null;

        private boolean allowMetadataChange = true;

        private Supplier<Node> monitorSupplier = BasicNode::new;

        private Predicate<DataSetRow> filter = r -> true;

        private Function<RowMetadata, Predicate<DataSetRow>> outFilter = metadata -> r -> true;

        private boolean globalStatistics = true;

        /** Limit the output number of rows. */
        private Long limit = null;

        public Builder monitor(Supplier<Node> monitorSupplier) {
            this.monitorSupplier = monitorSupplier;
            return this;
        }

        /**
         * @param output where to write the transformed dataset.
         * @return the mapper to chain calls.
         */
        public Builder output(final OutputStream output) {
            this.output = output;
            return this;
        }

        /**
         * @return a new {@link Configuration} from the mapper setup.
         */
        public Configuration build() {
            return new Configuration(output, filter, outFilter, monitorSupplier, sourceType, format, actions, arguments,
                    preparation, stepId, allowMetadataChange, globalStatistics, dataVolume, limit);
        }

        /**
         * Builder DSL for format setter
         *
         * @param format The format type id.
         * @return The mapper
         */
        public Builder format(final String format) {
            this.format = format;
            return this;
        }

        /**
         * Builder DSL for format setter
         *
         * @param sourceType The source type.
         * @return The mapper
         */
        public Builder sourceType(final ExportParameters.SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        /**
         * Builder DSL for actions setter
         *
         * @param actions The actions in JSON string format.
         * @return The mapper
         */
        public Builder actions(final String actions) {
            this.actions = actions;
            return this;
        }

        /**
         * Builder DSL for arguments setter
         *
         * @param arguments The arguments in Map
         * @return The mapper
         */
        public Builder args(final Map<String, String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder stepId(final String stepId) {
            if (StringUtils.equalsIgnoreCase("head", stepId)) {
                throw new IllegalArgumentException("'head' is not a valid step id.");
            }
            this.stepId = stepId;
            return this;
        }

        public Builder preparation(final PreparationMessage preparation) {
            this.preparation = preparation;
            return this;
        }

        public Builder volume(Volume dataVolume) {
            this.dataVolume = dataVolume;
            return this;
        }

        public Builder allowMetadataChange(boolean allowMetadataChange) {
            this.allowMetadataChange = allowMetadataChange;
            return this;
        }

        public Builder globalStatistics(boolean globalStatistics) {
            this.globalStatistics = globalStatistics;
            return this;
        }

        public Builder inFilter(Predicate<DataSetRow> filter) {
            this.filter = filter;
            return this;
        }

        public Builder outFilter(Function<RowMetadata, Predicate<DataSetRow>> outFilter) {
            this.outFilter = outFilter;
            return this;
        }

        public Builder limit(Long limit) {
            this.limit = limit;
            return this;
        }
    }
}
