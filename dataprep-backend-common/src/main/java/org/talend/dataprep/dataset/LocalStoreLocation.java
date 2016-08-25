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

package org.talend.dataprep.dataset;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
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

    /**
     * @see DataSetLocation#getLocationType()
     */
    @Override
    public String getLocationType() {
        return NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(new Parameter("datasetFile", ParameterType.FILE, "", false, false, "*.csv"));
    }

    @Override
    public String getAcceptedContentType() {
        return " text/plain";
    }

    @Override
    public String toMediaType(FormatFamily formatFamily) {
        return formatFamily.getMediaType();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {

        // since there's no field to compare, the comparison is only performed on the class

        if (this == o) {
            return true;
        }
        return !(o == null || getClass() != o.getClass());
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

}
