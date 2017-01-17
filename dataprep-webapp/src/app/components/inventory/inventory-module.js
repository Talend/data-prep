/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import INVENTORY_COPY_MOVE_MODULE from './copy-move/inventory-copy-move-module';
import INVENTORY_HEADER_MODULE from './header/inventory-header-module';
import INVENTORY_ITEM_MODULE from './item/inventory-item-module';
import INVENTORY_TILE_MODULE from './tile/inventory-tile-module';


const MODULE_NAME = 'data-prep.inventory';

export default MODULE_NAME;

angular.module(MODULE_NAME,
	[
		INVENTORY_COPY_MOVE_MODULE,
		INVENTORY_HEADER_MODULE,
		INVENTORY_ITEM_MODULE,
		INVENTORY_TILE_MODULE,
	]);
