package org.talend.dataprep.transformation.api.transformer.suggestion;

import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

public class Suggestion {

    private ActionMetadata action;

    private int score;

    public Suggestion(ActionMetadata action, int score) {
        this.action = action;
        this.score = score;
    }

    public ActionMetadata getAction() {
        return action;
    }

    public void setAction(ActionMetadata action) {
        this.action = action;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Suggestion{" + "action=" + action + ", score=" + score + '}';
    }
}
