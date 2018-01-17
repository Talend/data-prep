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

package org.talend.dataprep.api.service.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simple bean used to display a dataset metadata and a summary of its related preparations.
 */
@JsonIgnoreProperties({ "lifecycle", "nbLinesHeader", "nbLinesFooter" })
public class EnrichedDataSetMetadata extends UserDataSetMetadata {

    /** For the Serialization interface. */
    private static final long serialVersionUID = 1;

    /** List of preparations based on this dataset. */
    private List<PreparationSummary> preparations;

    /**
     * Constructor.
     *
     * @param source the original dataset metadata.
     * @param relatedPreparations the relatedPreparations based on this dataset.
     */
    public EnrichedDataSetMetadata(UserDataSetMetadata source, List<Preparation> relatedPreparations) {
        this.setId(source.getId());
        this.setFavorite(source.isFavorite());
        this.setName(source.getName());
        this.setAuthor(source.getAuthor());
        this.setCreationDate(source.getCreationDate());
        this.setLastModificationDate(source.getLastModificationDate());
        this.setContent(source.getContent());
        this.setDraft(source.isDraft());
        this.setEncoding(source.getEncoding());
        this.setLocation(source.getLocation());
        this.setSheetName(source.getSheetName());
        this.setTag(source.getTag());
        this.getGovernance().setCertificationStep(source.getGovernance().getCertificationStep());

        this.setSchemaParserResult(null); // not interested
        this.getContent().getParameters().clear(); // not interested

        if (relatedPreparations != null) {
            this.preparations = relatedPreparations.stream().map(PreparationSummary::new).collect(Collectors.toList());
        } else {
            this.preparations = Collections.emptyList();
        }
        this.setOwner(source.getOwner());
        this.setSharedByMe(source.isSharedByMe());
        this.setSharedResource(source.isSharedDataSet());
        this.setRoles(source.getRoles());
    }

    public List<PreparationSummary> getPreparations() {
        return preparations;
    }

    public void setPreparations(List<PreparationSummary> preparations) {
        this.preparations = preparations;
    }

    @Override
    public String toString() {
        return "EnrichedDataSetMetadata{" + //
                "id=" + this.getId() + //
                ", name=" + this.getName() + //
                ", preparations=" + preparations //
                + "} " + super.toString();
    }

    /**
     * Bean used to model a preparation based on this dataset.
     */
    public static class PreparationSummary implements Serializable {

        /** For the Serialization interface. */
        private static final long serialVersionUID = 1;

        /** The preparation id. */
        private String id;

        /** The preparation name. */
        private String name;

        /** The number of steps within this preparation. */
        private int nbSteps;

        /** The preparation last modification date. */
        private long lastModificationDate;

        /**
         * Constructor.
         *
         * @param source the preparation source.
         */
        public PreparationSummary(Preparation source) {
            this.id = source.getId();
            this.name = source.getName();
            this.lastModificationDate = source.getLastModificationDate();
            this.nbSteps = source.getSteps().size();
        }

        public String getId() {
            return id;
        }


        public void setId(String id) {
            this.id = id;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNbSteps() {
            return nbSteps;
        }

        public void setNbSteps(int nbSteps) {
            this.nbSteps = nbSteps;
        }

        public long getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(long lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }

        @Override
        public String toString() {
            return "PreparationSummary{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", nbSteps=" + nbSteps
                    + ", lastModificationDate=" + lastModificationDate + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PreparationSummary that = (PreparationSummary) o;
            return nbSteps == that.nbSteps && lastModificationDate == that.lastModificationDate && Objects.equals(id, that.id)
                    && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, nbSteps, lastModificationDate);
        }
    }
}
