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

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.settings.AppSettings;
import org.talend.dataprep.api.service.settings.AppSettingsService;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import io.swagger.annotations.ApiOperation;

/**
 * App settings API
 */
@RestController
public class AppSettingsAPI extends APIService {

    @Autowired
    private AppSettingsService appSettingsService;

    /**
     * Returns the app settings to configure frontend components
     */
    @RequestMapping(value = "/api/settings", method = GET)
    @ApiOperation(value = "Get the app settings", produces = APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public Callable<AppSettings> getSettings() {
        return () -> appSettingsService.getSettings();
    }
}
