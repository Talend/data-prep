/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import inventoryTile from './inventory-tile-directive';

const MODULE_NAME = 'data-prep.inventory-tile';

/**
 * @ngdoc object
 * @name data-prep.inventory-tile
 * @description This module contains the inventory tile used for preparation display
 */
angular.module(MODULE_NAME, [SERVICES_UTILS_MODULE])
    .directive('inventoryTile', inventoryTile);

export default MODULE_NAME;
