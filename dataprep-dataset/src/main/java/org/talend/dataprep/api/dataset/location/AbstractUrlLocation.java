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

package org.talend.dataprep.api.dataset.location;

import java.io.Serializable;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for all Url related dataset location.
 */
public abstract class AbstractUrlLocation implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset http url. */
    @JsonProperty("url")
    protected String url;

    /**
     * @return the Url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Encode and set the given url.
     * 
     * @param url the url to set.
     */
    public void setUrl(String url) throws URIException {
        if (StringUtils.isNotBlank(url)) {
            this.url = URIUtil.encodeQuery(url);
        } else {
            this.url = url;
        }
    }
}
