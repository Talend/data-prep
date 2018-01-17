/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './inventory-tile.html';

/**
 * @ngdoc directive
 * @name data-prep.inventory-tile.directive:InventoryTile
 * @description This directive display an preparation tile
 * @restrict E
 * @usage
 * <inventory-tile
 *      dataset="preparation.dataset"
 *      on-click="angular.noop"
 *      on-clone="preparationListCtrl.clone(preparation)"
 *      on-favorite="angular.noop"
 *      on-remove="preparationListCtrl.remove(preparation)"
 *      on-rename="preparationListCtrl.rename(preparation, text)"
 *      on-title-click="preparationListCtrl.load(preparation)"
 *      preparation="preparation">
 * </inventory-tile>
 *
 * @param {Object}      dataset base dataset of the preparation
 * @param {Object}      preparation displayed preparation on the tile
 * @param {function}    onClick callback function when the whole tile is clicked
 * @param {function}    onClone clones a preparation
 * @param {function}    onFavorite sets a preparation a favorite
 * @param {function}    onRemove removes a preparation
 * @param {function}    onRename callback function to rename a preparation
 * @param {function}    onTitleClick callback function when the title is clocked
 */
export default function inventoryTile() {
	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'inventoryTileCtrl',
		controller: () => {
		},

		scope: {
			dataset: '<',
			preparation: '<',
			onClick: '&',
			onClone: '&',
			onFavorite: '&',
			onRemove: '&',
			onRename: '&',
			onTitleClick: '&',
		},
		link: (scope, iElement, iAttrs, ctrl) => {
			ctrl.editableTitle = iAttrs.onRename;
			ctrl.showFavoriteIcon = iAttrs.onFavorite;
			ctrl.showCloneIcon = iAttrs.onClone;
			ctrl.showRemoveIcon = iAttrs.onRemove;
		},
	};
}
