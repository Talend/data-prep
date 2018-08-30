// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.async;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class AsyncExecutionResult {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncExecutionResult.class);

    /** When the full run was finished. */
    private final long endDate = System.currentTimeMillis();

    @JsonProperty("properties")
    @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_NULL)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final Map<String, String> properties = Collections.synchronizedMap(new LinkedHashMap<>());

    /** The full run content type. */
    private String type;

    /**
     * Url where we can get the result of the asynchrone execution
     */
    private String downloadUrl;

    // for JSON Serialization
    public AsyncExecutionResult() {
    }

    public AsyncExecutionResult(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        if (properties.containsKey("downloadUrl")) {
            return properties.get("downloadUrl");
        }
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getEndDate() {
        return endDate;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

}
