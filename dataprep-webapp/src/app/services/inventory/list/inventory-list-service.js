/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const DATA_FEATURE_ATTR = 'data-feature';

export const DATA_FEATURE_RENAME = 'inventory.rename';
export const DATA_FEATURE_FAVORITE = 'dataset.favorite';

/**
 * @ngdoc service
 * @name data-prep.services.inventory.list:InventoryListService
 * @description
 * {@link data-prep.services.inventory.service:InventoryService InventoryService} must be the only entry point for <b>inventory</b>
 */
export default function DatasetListService() {
	'ngInject';

	return {
		adaptAction,
	};

	/**
	 * @ngdoc method
	 * @name adaptRenameAction
	 * @methodOf data-prep.services.inventory.list:InventoryListService
	 * @description Adapt inventory item rename action
	 * @param adaptedAction action to deal with
	 * @param actionDataFeature action data-feature value
	 * @param model inventory item model
	 */
	function adaptRenameAction(adaptedAction, actionDataFeature, model) {
		if (model.type && actionDataFeature === DATA_FEATURE_RENAME) {
			adaptedAction[DATA_FEATURE_ATTR] = `${model.type}.rename`;
		}
	}

	/**
	 * @ngdoc method
	 * @name adaptFavoriteAction
	 * @methodOf data-prep.services.inventory.list:InventoryListService
	 * @description Adapt inventory item favorite action
	 * @param adaptedAction action to deal with
	 * @param actionDataFeature action data-feature value
	 * @param model inventory item model
	 */
	function adaptFavoriteAction(adaptedAction, actionDataFeature, model) {
		if (model.favorite && actionDataFeature === `${DATA_FEATURE_FAVORITE}.add`) {
			adaptedAction[DATA_FEATURE_ATTR] = `${DATA_FEATURE_FAVORITE}.remove`;
		}
	}

	/**
	 * @ngdoc method
	 * @name adaptAction
	 * @methodOf data-prep.services.inventory.list:InventoryListService
	 * @description Adapt inventory item action
	 * @param action The action to adapt
	 * @param hostModel The inventory item model
	 * @returns {object} adaptedAction The adapted action
	 */
	function adaptAction(action, hostModel) {
		console.log('adaptAction', { action, hostModel });
		const adaptedAction = { ...action };
		const actionDataFeature = adaptedAction[DATA_FEATURE_ATTR];
		// Rename inventory item
		adaptRenameAction(adaptedAction, actionDataFeature, hostModel);

		const model = hostModel && hostModel.model;
		if (model) {
			// Favorite action for a dataset
			adaptFavoriteAction(adaptedAction, actionDataFeature, model);
		}
		return adaptedAction;
	}
}
