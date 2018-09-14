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

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.settings.AppSettings;
import org.talend.dataprep.api.service.settings.AppSettingsService;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.security.Security;

import io.swagger.annotations.ApiOperation;

/**
 * App settings API
 */
@RestController
public class AppSettingsAPI extends APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettingsAPI.class);

    @Autowired
    private AppSettingsService appSettingsService;

    @Autowired
    private Security security;

    /**
     * Returns the app settings to configure frontend components
     */
    @RequestMapping(value = "/api/settings", method = GET)
    @ApiOperation(value = "Get the app settings", produces = APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public Callable<AppSettings>
            getSettings(@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String language) {
        return () -> {
            if (StringUtils.isBlank(language)) {
                final Locale userLocale = security.getLocale();
                final Locale previous = LocaleContextHolder.getLocale();
                LocaleContextHolder.setLocale(userLocale);
                LOGGER.info("No request locale, locale changed from {} to {}.", previous, userLocale);
            }
            return context.getBean(AppSettingsService.class).getSettings();
        };
    }
}
