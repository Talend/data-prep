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

package org.talend.dataprep.api.dataset.location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.schema.FormatFamily;

/**
 * Location used for remote hdfs dataset.
 */
@Component
public class HdfsLocation extends AbstractUrlLocation implements DataSetLocation {

    /** DataSet media type for remote hdfs datasets. */
    public static final String MEDIA_TYPE = "application/vnd.remote-ds.hdfs";

    /** Name of the http location. */
    public static final String NAME = "hdfs";

    /**
     * @see DataSetLocation#getLocationType()
     */
    @Override
    public String getLocationType() {
        return NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(
                new Parameter("name", ParameterType.STRING, "", false, false), //
                new Parameter("url", ParameterType.STRING, "", false, false, "hdfs://host:port/file")
        );
    }

    @Override
    public String getAcceptedContentType() {
        return MEDIA_TYPE;
    }

    @Override
    public String toMediaType(FormatFamily formatFamily) {
        return formatFamily.getMediaType();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "HdfsLocation{" + "url='" + url + '\'' + '}';
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HdfsLocation that = (HdfsLocation) o;
        return Objects.equals(url, that.url);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
