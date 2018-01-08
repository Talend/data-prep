// ============================================================================
//
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

package org.talend.dataprep.api.dataset;

import static java.util.Collections.emptyList;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.row.FlagNames;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information about a column in a data set. It includes:
 * <ul>
 * <li>Name ({@link #getId()})</li>
 * <li>Type ({@link #getType()})</li>
 * </ul>
 * 
 * @see ColumnMetadata.Builder
 */
public class ColumnMetadata implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Quality of the column. */
    @JsonProperty("quality")
    private final Quality quality = new Quality();

    /** Technical id of the column (generated when instantiated). */
    @JsonProperty("id")
    private String id;

    /** Human readable name of the column. */
    private String name;

    /** Type of the column (N/A as default). */
    @JsonProperty("type")
    private String typeName = "N/A"; //$NON-NLS-1$

    /** Number of first lines with a text header (none per default). */
    private int headerSize = 0;

    /** Optional diff flag that shows diff status of this column metadata. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = FlagNames.COLUMN_DIFF_KEY)
    private String diffFlagValue;

    /** Statistics of the column. */
    @JsonProperty("statistics")
    private Statistics statistics = new Statistics();

    @JsonProperty("domain")
    private String domain = StringUtils.EMPTY;

    @JsonProperty("domainLabel")
    private String domainLabel = StringUtils.EMPTY;

    @JsonProperty("domainFrequency")
    private float domainFrequency;

    @JsonProperty("semanticDomains")
    private List<SemanticDomain> semanticDomains = emptyList();

    /** if the domain has been changed/forced manually by the user */
    @JsonProperty("domainForced")
    private boolean domainForced;

    /** if the type has been changed/forced manually by the user */
    @JsonProperty("typeForced")
    private boolean typeForced;

    /**
     * Default empty constructor.
     */
    public ColumnMetadata() {
        // no op
    }

    /**
     * Create a column metadata from the given parameters.
     *
     * @param name the column name.
     * @param typeName the column type.
     */
    private ColumnMetadata(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    /**
     * @return The column name. It never returns <code>null</code> or empty string.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the column name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The column's type. It never returns <code>null</code>.
     * @see org.talend.dataprep.api.type.Type
     */
    public String getType() {
        return typeName;
    }

    /**
     * @param typeName the typename to set.
     */
    public void setType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return the diff flag.
     */
    public String getDiffFlagValue() {
        return diffFlagValue;
    }

    /**
     * @param diffFlagValue the diff flag to set.
     */
    public void setDiffFlagValue(String diffFlagValue) {
        this.diffFlagValue = diffFlagValue;
    }

    /**
     * @return the header size.
     */
    public int getHeaderSize() {
        return headerSize;
    }

    /**
     * @param headerSize the header size to set.
     */
    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    /**
     * @return the quality.
     */
    public Quality getQuality() {
        return quality;
    }

    public boolean isDomainForced() {
        return domainForced;
    }

    public void setDomainForced(boolean domainForced) {
        this.domainForced = domainForced;
    }

    public boolean isTypeForced() {
        return typeForced;
    }

    public void setTypeForced(boolean typeForced) {
        this.typeForced = typeForced;
    }

    /**
     * Returns true if this column metadata is compatible (has same type and the same name) with <tt>columnMetadata</tt>
     * and false otherwise.
     *
     * @param columnMetadata the specified row metadata
     * @return true if this column metadata is compatible <tt>columnMetadata</tt> and false otherwise
     */
    public boolean compatible(ColumnMetadata columnMetadata) {
        if (columnMetadata != null) {
            return StringUtils.equals(typeName, columnMetadata.getType())
                    && StringUtils.equalsIgnoreCase(name, columnMetadata.getName());
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj) //Colum
                .filter(that -> that instanceof ColumnMetadata) //
                .map(that -> (ColumnMetadata) that) //
                .filter(that -> Objects.equals(this.getDiffFlagValue(), that.getDiffFlagValue())) //
                .filter(that -> Objects.equals(this.getId(), that.getId())) //
                .filter(that -> Objects.equals(this.getName(), that.getName())) //
                .filter(that -> Objects.equals(this.getType(), that.getType())) //
                .isPresent();
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getType().hashCode();
        if (name != null) {
            result = 31 * result + getName().hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "ColumnMetadata{" + //
                "id='" + id + '\'' + //
                ", name='" + name + '\'' + //
                ", typeName='" + typeName + '\'' + //
                ", quality=" + quality + //
                ", headerSize=" + headerSize + //
                ", diffFlagValue='" + diffFlagValue + '\'' + //
                ", statistics='" + statistics + '\'' + //
                ", domain='" + domain + '\'' + //
                ", domainLabel='" + domainLabel + '\'' + //
                ", semanticDomains=" + semanticDomains + //
                '}';
    }

    /**
     * @return The statistics returned by data quality library.
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the statistics as returned by data quality library.
     *
     * @param statistics The statistics as returned by the data quality library.
     */
    public void setStatistics(Statistics statistics) {
        if (statistics == null) {
            this.statistics = new Statistics();
        } else {
            this.statistics = statistics;
        }
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public String getDomainLabel() {
        return domainLabel;
    }

    public void setDomainLabel(String domainLabel) {
        this.domainLabel = domainLabel;
    }

    public List<SemanticDomain> getSemanticDomains() {
        return semanticDomains;
    }

    public void setSemanticDomains(List<SemanticDomain> semanticDomains) {
        this.semanticDomains = semanticDomains;
    }

    public float getDomainFrequency() {
        return domainFrequency;
    }

    public void setDomainFrequency(float domainFrequency) {
        this.domainFrequency = domainFrequency;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * This class builder to ease the constructor.
     */
    public static class Builder {

        /** The column id. */
        private String id;

        /** The column name. */
        private String name;

        /** The column type. */
        private Type type;

        /** The column empty value. */
        private int empty;

        /** The column invalid value count. */
        private int invalid;

        /** The column valid value count. */
        private int valid;

        /** The column header size. */
        private int headerSize;

        /** The column diff flag (null by default). */
        private String diffFlagValue = null;

        /** The column statistics. */
        private Statistics statistics;

        private String domain;

        private String domainLabel;

        private float domainFrequency;

        private List<SemanticDomain> semanticDomains;

        private boolean domainForced;

        private boolean typeForced;

        /**
         * @return A ColumnMetadata builder.
         */
        public static ColumnMetadata.Builder column() {
            return new Builder();
        }

        /**
         * Set the name of the column.
         * 
         * @param name the name of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the id of the column.
         * 
         * @param id the id of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder id(int id) {
            DecimalFormat format = new DecimalFormat("0000"); //$NON-NLS-1$
            return computedId(format.format(id));
        }

        /**
         * Set the computed id of the column.
         *
         * @param computedId the computed id of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder computedId(String computedId) {
            this.id = computedId;
            return this;
        }

        /**
         * Set the column statistics.
         *
         * @param statistics the column statistics to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder statistics(Statistics statistics) {
            this.statistics = statistics;
            return this;
        }

        /**
         * Set the type of the column.
         * 
         * @param type the type of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder type(Type type) {
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null.");
            }
            this.type = type;
            return this;
        }

        /**
         * Set the empty value of the column.
         * 
         * @param value the empty value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder empty(int value) {
            empty = value;
            return this;
        }

        /**
         * Set the invalid value of the column.
         * 
         * @param value the invalid value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder invalid(int value) {
            invalid = value;
            return this;
        }

        /**
         * Set the valid value of the column.
         * 
         * @param value the valid value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder valid(int value) {
            valid = value;
            return this;
        }

        /**
         * Set the header size value of the column.
         * 
         * @param headerSize the header size value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder headerSize(int headerSize) {
            this.headerSize = headerSize;
            return this;
        }

        /**
         *
         * @param domain the domain value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         *
         * @param domainLabel the domain label value of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder domainLabel(String domainLabel) {
            this.domainLabel = domainLabel;
            return this;
        }

        /**
         *
         * @param domainFrequency the frequency of value with this domain of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder domainFrequency(float domainFrequency) {
            this.domainFrequency = domainFrequency;
            return this;
        }

        /**
         *
         * @param semanticDomains the semantic domains of the column to set.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder semanticDomains(List<SemanticDomain> semanticDomains) {
            this.semanticDomains = semanticDomains;
            return this;
        }

        /**
         *
         * @param semanticDomainForced if semantic domain has been forced on this column.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder semanticDomainForce(boolean semanticDomainForced) {
            this.domainForced = semanticDomainForced;
            return this;
        }

        /**
         *
         * @param typeForced if type has been forced on this column.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder typeForce(boolean typeForced) {
            this.typeForced = typeForced;
            return this;
        }

        /**
         * Copy the column from the given one.
         * 
         * @param original the column to copy.
         * @return the builder to carry on building the column.
         */
        public ColumnMetadata.Builder copy(ColumnMetadata original) {
            this.id = original.getId();
            this.name = original.getName();
            Quality originalQuality = original.getQuality();
            this.empty = originalQuality.getEmpty();
            this.invalid = originalQuality.getInvalid();
            this.valid = originalQuality.getValid();
            this.headerSize = original.getHeaderSize();
            this.type = Type.get(original.getType());
            this.diffFlagValue = original.getDiffFlagValue();
            this.statistics = original.getStatistics();
            this.domain = original.getDomain();
            this.domainLabel = original.getDomainLabel();
            this.domainFrequency = original.getDomainFrequency();
            this.semanticDomains = original.getSemanticDomains();
            this.domainForced = original.isDomainForced();
            this.typeForced = original.isTypeForced();
            return this;
        }

        /**
         * Copy the column metadata type (without statistics)
         * 
         * @param original the original column to copy.
         * @return the column metadata that only match the type.
         */
        public ColumnMetadata.Builder copyMatchingEmptyColumnMetadata(ColumnMetadata original) {
            this.id = original.getId();
            this.name = original.getName();
            this.empty = 0;
            this.invalid = 0;
            this.valid = 0;
            this.type = Type.get(original.getType());
            this.domain = original.getDomain();
            this.domainLabel = original.getDomainLabel();
            this.semanticDomains = original.getSemanticDomains();
            return this;
        }

        /**
         * Build the column with the previously entered values.
         * 
         * @return the built column metadata.
         */
        public ColumnMetadata build() {
            ColumnMetadata columnMetadata;
            columnMetadata = new ColumnMetadata(name, type.getName());
            columnMetadata.setId(id);
            columnMetadata.getQuality().setEmpty(empty);
            columnMetadata.getQuality().setInvalid(invalid);
            columnMetadata.getQuality().setValid(valid);
            columnMetadata.setHeaderSize(this.headerSize);
            columnMetadata.setDiffFlagValue(this.diffFlagValue);
            columnMetadata.setStatistics(this.statistics == null ? new Statistics() : this.statistics);
            columnMetadata.setDomain(this.domain == null ? StringUtils.EMPTY : this.domain);
            columnMetadata.setDomainLabel(this.domainLabel == null ? StringUtils.EMPTY : this.domainLabel);
            columnMetadata.setDomainFrequency(this.domainFrequency);
            columnMetadata.setSemanticDomains(this.semanticDomains == null ? emptyList() : this.semanticDomains);
            columnMetadata.setDomainForced(this.domainForced);
            columnMetadata.setTypeForced(this.typeForced);
            return columnMetadata;
        }
    }
}
