//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dataset content summary.
 */
public class DataSetContent implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("records")
    private long nbRecords = 0;

    @JsonProperty("nbLinesHeader")
    private int nbLinesInHeader;

    @JsonProperty("nbLinesFooter")
    private int nbLinesInFooter;

    @JsonProperty("type")
    private String mediaType;

    @JsonProperty("parameters")
    private Map<String, String> parameters = new HashMap<>();

    @JsonProperty("formatGuess")
    private String formatGuessId;

    /** If the dataset is too big, */
    @JsonProperty("limit")
    @JsonInclude(value = NON_ABSENT, content = NON_ABSENT)
    private Optional<Long> limit = Optional.empty();

    /**
     * @return A map that contains additional information about the format (e.g. a separator for a CSV format).
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getFormatGuessId() {
        return formatGuessId;
    }

    public void setFormatGuessId(String formatGuessId) {
        this.formatGuessId = formatGuessId;
    }

    public long getNbRecords() {
        return this.nbRecords;
    }

    public void setNbRecords(long lines) {
        this.nbRecords = lines;
    }

    public int getNbLinesInHeader() {
        return this.nbLinesInHeader;
    }

    public void setNbLinesInHeader(int nbLinesInHeader) {
        this.nbLinesInHeader = nbLinesInHeader;
    }

    public int getNbLinesInFooter() {
        return this.nbLinesInFooter;
    }

    public void setNbLinesInFooter(int nbLinesInFooter) {
        this.nbLinesInFooter = nbLinesInFooter;
    }

    /**
     * @return the Limit
     */
    public Optional<Long> getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set.
     */
    public void setLimit(Long limit) {
        if (limit != null) {
            this.limit = Optional.of(limit);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSetContent that = (DataSetContent) o;
        return Objects.equals(nbRecords, that.nbRecords) && //
                Objects.equals(nbLinesInHeader, that.nbLinesInHeader) && //
                Objects.equals(nbLinesInFooter, that.nbLinesInFooter) && //
                Objects.equals(limit, that.limit) && //
                Objects.equals(mediaType, that.mediaType) && //
                Objects.equals(parameters, that.parameters) && //
                Objects.equals(formatGuessId, that.formatGuessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nbRecords, nbLinesInHeader, nbLinesInFooter, limit, mediaType, parameters, formatGuessId);
    }
}
