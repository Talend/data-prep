/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngSanitize from 'angular-sanitize';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import UpgradeVersionComponent from './upgrade-version-component';
import UpgradeVersionService from './upgrade-version-service';

const MODULE_NAME = 'data-prep.upgrade-version';

/**
 * @ngdoc object
 * @name data-prep.upgrade-version
 * @description This module contains the entities to display an upgrade version message.
 * @requires data-prep.services.state
 * @requires data-prep.services.folder
 */
angular.module(MODULE_NAME, [ngSanitize, SERVICES_UTILS_MODULE])
    .component('upgradeVersion', UpgradeVersionComponent)
    .service('UpgradeVersionService', UpgradeVersionService);

export default MODULE_NAME;
