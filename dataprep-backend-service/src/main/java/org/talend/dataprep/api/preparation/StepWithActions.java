/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.api.preparation;

import java.util.function.Function;

/**
 * Representation of a Step without using identifiers to target another element.
 */
public class StepWithActions {

    private String id;

    private StepWithActions parent;

    private PreparationActions content;

    private String appVersion;

    private StepDiff diff;

    public static StepWithActions buildFromStep(Step step, Function<String, PreparationActions> contentSource,
                                                Function<String, Step> stepSource) {
        StepWithActions swa;
        if (step == null) {
            swa = null;
        } else {
            swa = new StepWithActions();
            swa.setId(step.getId());
            if (step.getParent() != null) {
                swa.setParent(buildFromStep(stepSource.apply(step.getParent()), contentSource, stepSource));
            }
            swa.setAppVersion(step.getAppVersion());
            swa.setContent(contentSource.apply(step.getContent()));
            swa.setDiff(step.getDiff());
        }
        return swa;
    }

    public static StepWithActions buildFromStepId(String stepId, Function<String, PreparationActions> contentSource,
                                                  Function<String, Step> stepSource) {
        Step step = stepSource.apply(stepId);
        return buildFromStep(step, contentSource, stepSource);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StepWithActions getParent() {
        return parent;
    }

    public void setParent(StepWithActions parent) {
        this.parent = parent;
    }

    public PreparationActions getContent() {
        return content;
    }

    public void setContent(PreparationActions content) {
        this.content = content;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public StepDiff getDiff() {
        return diff;
    }

    public void setDiff(StepDiff diff) {
        this.diff = diff;
    }
}
