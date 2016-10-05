/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';
import INVENTORY_ITEM_MODULE from '../../inventory/item/inventory-item-module';
import INVENTORY_TILE_MODULE from '../../inventory/tile/inventory-tile-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_FOLDER_MODULE from '../../../services/folder/folder-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';
import SERVICES_PREPARATION_MODULE from '../../../services/preparation/preparation-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import PreparationListComponent from './preparation-list-component';

const MODULE_NAME = 'data-prep.preparation-list';

/**
 * @ngdoc object
 * @name data-prep.preparation-list
 * @description This module contains the entities to manage the preparation list
 * @requires ui.router
 * @requires talend.widget
 * @requires data-prep.inventory-tile
 * @requires data-prep.services.folder
 * @requires data-prep.services.preparation
 * @requires data-prep.services.playground
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		INVENTORY_ITEM_MODULE,
		INVENTORY_TILE_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_FOLDER_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_STATE_MODULE,
	])
    .component('preparationList', PreparationListComponent);

export default MODULE_NAME;
