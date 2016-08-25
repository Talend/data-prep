// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.i18n;

class ActionMessagesDelegate {

    private static final String ACTION_PREFIX = "action.";

    private static final String DESCRIPTION_SUFFIX = ".desc";

    private static final String LABEL_SUFFIX = ".label";

    private static final String PARAMETER_PREFIX = "parameter.";

    private static final String CHOICE_PREFIX = "choice.";

    private ActionMessagesDelegate() {
    }

    public static String getActionLabelKey(String actionName) {
        return ACTION_PREFIX + actionName + LABEL_SUFFIX;
    }

    public static String getActionDescriptionKey(String actionName) {
        return ACTION_PREFIX + actionName + DESCRIPTION_SUFFIX;
    }

    public static String getParameterLabelKey(String parameterName) {
        return PARAMETER_PREFIX + parameterName + LABEL_SUFFIX;
    }

    public static String getParameterDescriptionKey(String parameterName) {
        return PARAMETER_PREFIX + parameterName + DESCRIPTION_SUFFIX;
    }

    public static String getChoiceKey(String choiceName) {
        return CHOICE_PREFIX + choiceName;
    }

}
