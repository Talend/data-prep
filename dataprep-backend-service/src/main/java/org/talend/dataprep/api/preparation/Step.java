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

package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Represents one step of a {@link Preparation}.
 */
public class Step extends Identifiable implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    private String parentId = StringUtils.EMPTY;

    private String contentId;

    /** The app version. */
    @JsonProperty("app-version")
    private String appVersion;

    private StepDiff diff;

    //@formatter:off
    @JsonCreator
    public Step(@JsonProperty("parentId") final String parentId, //
                @JsonProperty("contentId") final String contentId, //
                @JsonProperty("app-version") final String appVersion) {
       this(parentId, contentId, appVersion, null);
    }
    //@formatter:on

    @PersistenceConstructor
    public Step(final String parentId, final String contentId, final String appVersion, final StepDiff diff) {
        setParent(parentId);
        setContent(contentId);
        setDiff(diff);
        this.appVersion = appVersion;
    }

    public String getContent() {
        return contentId;
    }

    public void setContent(String content) {
        this.contentId = content;
    }

    public String getParent() {
        return parentId;
    }

    public void setParent(String parent) {
        this.parentId = parent;
    }

    public StepDiff getDiff() {
        return diff;
    }

    public void setDiff(StepDiff diff) {
        this.diff = diff;
    }

    /**
     * @return the AppVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        return DigestUtils.sha1Hex(parentId + contentId);
    }

    @Override
    public void setId(String id) {
        // No op
    }

    @Override
    public String toString() {
        return "Step{" + //
                "parentId='" + parentId + '\'' + //
                ", contentId='" + contentId + '\'' + //
                ", appVersion='" + appVersion + '\'' + //
                ", diff=" + diff + //
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Step step = (Step) o;
        return Objects.equals(parentId, step.parentId) && Objects.equals(contentId, step.contentId)
                && Objects.equals(appVersion, step.appVersion) && Objects.equals(diff, step.diff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, contentId, appVersion, diff);
    }
}
