package org.talend.dataprep.api.service;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.MixedContentMap;

public class EntityBuilder {

    public static MixedContentMap buildParametersMap(String... params) {
        MixedContentMap secondActionParams = new MixedContentMap();
        if (params.length > 0) {
            for (int i = 1; i < params.length; i += 2) {
                String param = params[i - 1];
                String value = params[i];
                secondActionParams.put(param, value);
            }
        }
        return secondActionParams;
    }

    public static Action buildAction(String secondActionName, MixedContentMap secondActionParams) {
        Action secondAction = new Action();
        secondAction.setName(secondActionName);
        secondAction.setParameters(secondActionParams);
        return secondAction;
    }

}
