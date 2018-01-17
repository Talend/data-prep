/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SETTINGS_ACTIONS_MODULE from './actions/settings-actions-module';

import { appSettings, SettingsService } from './settings-service';
import SettingsActionsService from './settings-actions-service';

const MODULE_NAME = 'app.settings';

angular.module(MODULE_NAME, [SETTINGS_ACTIONS_MODULE])
	.service('SettingsActionsService', SettingsActionsService)
	.service('SettingsService', SettingsService)
	.constant('appSettings', appSettings);

export default MODULE_NAME;
