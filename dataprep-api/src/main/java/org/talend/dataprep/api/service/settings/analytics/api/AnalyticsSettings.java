package org.talend.dataprep.api.service.settings.analytics.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Bean that models the Analytics settings.
 */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class AnalyticsSettings {

    /** The help property id. */
    private String id;

    /** The help property value. */
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the AnalyticsSettings builder.
     */
    public static AnalyticsSettings.Builder builder() {
        return new AnalyticsSettings.Builder();
    }

    public static AnalyticsSettings.Builder from(final AnalyticsSettings analyticsSettings) {
        return builder() //
                .id(analyticsSettings.getId()) //
                .value(analyticsSettings.getValue());
    }

    /**
     * AnalyticsSettings builder.
     */
    public static class Builder {

        private String id;

        private String value;

        public AnalyticsSettings.Builder id(String id) {
            this.id = id;
            return this;
        }

        public AnalyticsSettings.Builder value(final String value) {
            this.value = value;
            return this;
        }

        public AnalyticsSettings build() {
            final AnalyticsSettings analyticsSettings = new AnalyticsSettings();
            analyticsSettings.setId(this.id);
            analyticsSettings.setValue(this.value);
            return analyticsSettings;
        }
    }

}
