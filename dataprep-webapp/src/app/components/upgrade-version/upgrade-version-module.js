/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc object
 * @name data-prep.upgrade-version
 * @description This module contains the controller and directives to display an upgrade version message.
 * @requires data-prep.services.state
 * @requires data-prep.services.folder
 */

import UpgradeVersionComponent from './upgrade-version-component';
import UpgradeVersionService from './upgrade-version-service';

(() => {
    angular.module('data-prep.upgrade-version', ['data-prep.services.utils', 'ngSanitize'])
           .component('upgradeVersion', UpgradeVersionComponent)
           .service('UpgradeVersionService', UpgradeVersionService);
})();