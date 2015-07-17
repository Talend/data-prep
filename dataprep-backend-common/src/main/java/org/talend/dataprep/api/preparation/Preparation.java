package org.talend.dataprep.api.preparation;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

public class Preparation extends Identifiable {

    private String dataSetId;

    private String author;

    private String name;

    private long creationDate;

    private long lastModificationDate;

    private Step step;

    private List<String> steps;

    public Preparation() {
        this.creationDate = System.currentTimeMillis();
        this.lastModificationDate = this.creationDate;
    }

    public Preparation(String dataSetId, Step step) {
        this();
        this.dataSetId = dataSetId;
        this.step = step;
    }

    /**
     * Creates a default preparation (no author) for <code>dataSetId</code> at {@link Step#ROOT_STEP}.
     * @param dataSetId A data set id.
     * @return A {@link Preparation preparation} where head is set to {@link Step#ROOT_STEP root step}.
     */
    public static Preparation defaultPreparation(String dataSetId) {
        return new Preparation(dataSetId, Step.ROOT_STEP);
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        if (StringUtils.isEmpty(name)) {
            return DigestUtils.sha1Hex(dataSetId + author);
        }
        return DigestUtils.sha1Hex(dataSetId + author + name);
    }

    @Override
    public void setId(String id) {
        // No op
    }

    @Override
    public String toString() {
        return "Preparation {" + "id='" + id() + '\'' + ", dataSetId='" + dataSetId + '\'' + ", author='" + author + '\''
                + ", creationDate=" + creationDate + ", lastModificationDate=" + lastModificationDate + ", step=" + step + '}';
    }

    public void updateLastModificationDate() {
        this.lastModificationDate = System.currentTimeMillis();
    }

    public Preparation merge(Preparation other) {
        Preparation merge = new Preparation();
        merge.dataSetId = other.dataSetId != null ? other.dataSetId : dataSetId;
        merge.author = other.author != null ? other.author : author;
        merge.name = other.name != null ? other.name : name;
        merge.creationDate = min(other.creationDate, creationDate);
        merge.lastModificationDate = max(other.lastModificationDate, lastModificationDate);
        merge.step = other.step != null ? other.step : step;
        return merge;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preparation that = (Preparation) o;
        return Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(lastModificationDate, that.lastModificationDate) &&
                Objects.equals(dataSetId, that.dataSetId) &&
                Objects.equals(author, that.author) &&
                Objects.equals(name, that.name) &&
                Objects.equals(step, that.step);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(dataSetId, author, name, creationDate, lastModificationDate, step);
    }
}
