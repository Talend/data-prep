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

package org.talend.dataprep.parameters.jsonschema;

import java.util.List;

/**
 * Representation of front-end behavior description based on <a href="https://github.com/frassinier/rjsf-material-design/">https://github.com/frassinier/rjsf-material-design/</a>
 */
public class UiSchema {

    @com.fasterxml.jackson.annotation.JsonUnwrapped
    List<UiField> uiFields;

}
