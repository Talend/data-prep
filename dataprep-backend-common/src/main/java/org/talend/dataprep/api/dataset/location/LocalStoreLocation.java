//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset.location;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.jsonschema.ComponentProperties;
import org.talend.dataprep.schema.FormatFamily;

/**
 * Location used for local store.
 */
public class LocalStoreLocation implements DataSetLocation {

    /** Name of this store. */
    public static final String NAME = "local";

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String getLocationType() {
        return NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return Collections.singletonList(Parameter.parameter(locale).setName("datasetFile")
                .setType(ParameterType.FILE)
                .setDefaultValue("")
                .setCanBeBlank(false)
                .setPlaceHolder("*.csv")
                .build(this));
    }

    @Override
    public ComponentProperties getParametersAsSchema(Locale locale) { return null; }

    @Override
    public boolean isSchemaOriented() { return false; }

    @Override
    public String getAcceptedContentType() {
        return "text/plain";
    }

    @Override
    public String toMediaType(FormatFamily formatFamily) {
        return formatFamily.getMediaType();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {

        // since there's no field to compare, the comparison is only performed on the class

        if (this == o) {
            return true;
        }
        return !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

}
