package org.talend.dataprep.api.preparation;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

public class Step extends Identifiable {

    /**
     * The "root step": this is the initial step for all preparation (i.e. a newly created preparation has this root
     * step as head).
     */
    public static final Step ROOT_STEP = new Step(null, PreparationActions.ROOT_CONTENT.id());

    private String parentId = StringUtils.EMPTY;

    private String contentId;

    public Step(final String parentId, final String contentId) {
        setParent(parentId);
        setContent(contentId);
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
        return "Step {" + "id='" + id() + '\'' + ", parent='" + parentId + '\'' + ", content='" + contentId + '\'' + '}';
    }
}
