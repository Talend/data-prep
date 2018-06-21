package org.talend.dataprep.api.preparation;

import java.util.List;

import org.talend.dataprep.api.action.ActionForm;

public class PreparationDetailsDTO {

    /** The dataset id. */
    private String id;

    private String dataSetId;

    /** The creation date. */
    private long creationDate;

    /** The last modification date. */
    private long lastModificationDate;

    /** The preparation name. */
    private String name;

    private List<String> steps;

    private List<Action> actions;

    private String author;

    private List<ActionForm> metadata;

    private boolean allowDistributedRun;
    private boolean allowFullRun;

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

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public List<ActionForm> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<ActionForm> metadata) {
        this.metadata = metadata;
    }

    public boolean isAllowDistributedRun() {
        return allowDistributedRun;
    }

    public void setAllowDistributedRun(boolean allowDistributedRun) {
        this.allowDistributedRun = allowDistributedRun;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setAllowFullRun(boolean allowFullRun) {
        this.allowFullRun = allowFullRun;
    }

    public boolean isAllowFullRun() {
        return allowFullRun;
    }
}
