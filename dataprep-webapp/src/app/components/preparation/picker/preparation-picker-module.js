/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import INVENTORY_TILE from '../../inventory/tile/inventory-tile-module';

import PreparationPicker from './preparation-picker-component';

const MODULE_NAME = 'data-prep.preparation-picker';

/**
 * @ngdoc object
 * @name data-prep.preparation-picker
 * @description This module contains the preparation picker form
 * @requires data-prep.inventory-tile
 */

angular.module(MODULE_NAME, [INVENTORY_TILE])
    .component('preparationPicker', PreparationPicker);

export default MODULE_NAME;
