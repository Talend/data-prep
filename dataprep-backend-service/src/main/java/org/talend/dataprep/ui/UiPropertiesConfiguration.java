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

package org.talend.dataprep.ui;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(prefix = "dataprep.ui.configuration.remote", name = "enabled", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("dataprep.ui")
public class UiPropertiesConfiguration implements UiConfiguration {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = getLogger(UiPropertiesConfiguration.class);

    private final Theme theme = new Theme();

    @PostConstruct
    public void init() {
        LOGGER.info("UI configuration is retrieved from properties (dataprep.ui.theme.enabled)");
    }

    public boolean hasTheme() {
        return theme.isEnabled();
    }

    public static class Theme {

        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public Theme getTheme() {
        return theme;
    }
}
